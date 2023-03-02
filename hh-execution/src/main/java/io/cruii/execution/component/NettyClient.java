package io.cruii.execution.component;

import cn.hutool.core.io.FileUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import io.cruii.execution.config.NettyConfiguration;
import io.cruii.execution.feign.PushFeignService;
import io.cruii.model.BiliUser;
import io.cruii.pojo.dto.BiliTaskUserDTO;
import io.cruii.pojo.dto.PushMessageDTO;
import io.cruii.pojo.entity.TaskConfigDO;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2022/4/2
 */
@Slf4j
@Component
public class NettyClient implements CommandLineRunner {
    private final NettyConfiguration nettyConfig;

    public NettyClient(NettyConfiguration nettyConfig) {
        this.nettyConfig = nettyConfig;
    }

    private Bootstrap bootstrap;

    private volatile Channel clientChannel;

    @Override
    public void run(String... args) {
        bootstrap = new Bootstrap();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        // 自定义处理程序
                        socketChannel.pipeline().addLast("lineBasedFrameDecoder", new LineBasedFrameDecoder(1024));
                        socketChannel.pipeline().addLast("stringEncoder", new StringEncoder());
                        socketChannel.pipeline().addLast("stringDecoder", new StringDecoder());
                        socketChannel.pipeline().addLast("clientHandler", new ClientHandler(NettyClient.this));
                        socketChannel.pipeline().addLast("idleState", new IdleStateHandler(0, 0, 5));
                    }
                });
        connect();
    }

    public boolean connect() {
        if (clientChannel != null && clientChannel.isActive()) {
            return true;
        }

        // 绑定端口并同步等待

        ChannelFuture channelFuture = bootstrap.connect(nettyConfig.getHost(), nettyConfig.getPort());
        channelFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                clientChannel = channelFuture.channel();
                log.info("Connect to server successfully!");
            } else {
                log.error("Failed to connect to server, try connect after 10s");
                future.channel().eventLoop().schedule(() -> {
                    connect();
                }, 10, TimeUnit.SECONDS);

            }
        });
        return false;
    }
}

@Log4j2
class ClientHandler extends ChannelInboundHandlerAdapter {
    private final NettyClient nettyClient;

    private final TaskRunner taskRunner = SpringUtil.getApplicationContext().getBean(TaskRunner.class);

    private final PushFeignService pushFeignService = SpringUtil.getApplicationContext().getBean(PushFeignService.class);

    private static final BlockingQueue<String> PUSH_QUEUE = new LinkedBlockingQueue<>(3000);

    private static volatile ScheduledExecutorService scheduledExecutor;

    private void initScheduledExecutor() {
        if (scheduledExecutor == null) {
            synchronized (ClientHandler.class) {
                if (scheduledExecutor == null) {
                    scheduledExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                        Thread t = new Thread(r, "Client-Reconnect-1");
                        t.setDaemon(true);
                        return t;
                    });
                }
            }
        }
    }

    public ClientHandler(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
        new Thread(this::pushMessage).start();
    }

    /**
     * 连接到服务器时触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello netty.\n".getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.warn(">>> The server [{}] is disconnected. <<<", ctx.channel());
        ctx.pipeline().remove(this);
        ctx.channel().close();
        reconnection(ctx);
    }

    /**
     * 消息到来时触发
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.debug("[exe] Received message: {}", msg);
        TaskConfigDO taskConfigDO = JSONUtil.toBean(((String) msg), TaskConfigDO.class);
        String dedeuserid = taskConfigDO.getDedeuserid();
        taskRunner.run(taskConfigDO, result -> {
            if (result != null) {
                BiliUser biliUser = result.getBiliUser();
                int upgradeDays = result.getUpgradeDays();
                BiliTaskUserDTO biliTaskUserDTO = new BiliTaskUserDTO();
                biliTaskUserDTO
                        .setDedeuserid(String.valueOf(biliUser.getMid()))
                        .setUsername(biliUser.getName())
                        .setLevel(biliUser.getLevel())
                        .setCoins(String.valueOf(biliUser.getCoins()))
                        .setCurrentExp(biliUser.getLevelExp().getCurrentExp())
                        .setNextExp(biliUser.getLevelExp().getNextExp())
                        .setUpgradeDays(upgradeDays)
                        .setVipStatus(biliUser.getVip().getStatus())
                        .setLastRunTime(LocalDateTime.now())
                        .setIsLogin(true);
                ctx.channel().writeAndFlush(Unpooled.copiedBuffer((JSONUtil.toJsonStr(biliTaskUserDTO) + "\n").getBytes(StandardCharsets.UTF_8)));
            }
            // 加入到推送队列
            try {
                PUSH_QUEUE.put(dedeuserid + ":" + MDC.get("traceId"));
            } catch (InterruptedException e) {
                log.error("添加到推送队列异常: {}", dedeuserid, e);
                Thread.currentThread().interrupt();
            }
        });

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        // 将发送缓冲区的消息全部写到SocketChannel中
        ctx.flush();
    }

    /**
     * 发生异常时触发
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException) {
            log.warn("exceptionCaught:客户端[{}]和远程断开连接", ctx.channel().localAddress());
        } else {
            log.error(cause.getMessage());
        }
        ctx.pipeline().remove(this);
        ctx.channel().close();
        reconnection(ctx);
    }

    private void reconnection(ChannelHandlerContext ctx) {
        log.info("The client will reconnect after 3 seconds");
        initScheduledExecutor();

        scheduledExecutor.schedule(() -> {
            boolean connected = nettyClient.connect();
            if (connected) {
                log.info("Netty server connected.");
            } else {
                reconnection(ctx);
            }
        }, 3, TimeUnit.SECONDS);
    }

    private void pushMessage() {
        while (true) {
            try {
                String msg = PUSH_QUEUE.take();
                String dedeuserid = msg.split(":")[0];
                String traceId = msg.split(":")[1];

                // 日志收集
                String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
                File logFile = new File("logs/execution/all-" + date + ".log");
                String content = null;
                if (logFile.exists()) {
                    List<String> logs = FileUtil.readLines(logFile, StandardCharsets.UTF_8);
                    content = logs.stream()
                            .filter(line -> line.contains(traceId) && (line.contains("INFO") || line.contains("ERROR")))
                            .map(line -> line.split("\\|\\|")[1])
                            .collect(Collectors.joining("\n"));
                }
                PushMessageDTO message = new PushMessageDTO();
                message.setDedeuserid(dedeuserid);
                message.setContent(content);
                this.pushFeignService.push(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }
}
