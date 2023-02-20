package io.cruii.execution.component;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import io.cruii.execution.config.NettyConfiguration;
import io.cruii.model.BiliUser;
import io.cruii.model.custom.BiliUserAndDays;
import io.cruii.pojo.dto.BiliTaskUserDTO;
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
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
        // 绑定端口并同步等待
        try {
            ChannelFuture channelFuture = bootstrap.connect(nettyConfig.getHost(), nettyConfig.getPort());
            boolean notTimeout = channelFuture.awaitUninterruptibly(30, TimeUnit.SECONDS);
            clientChannel = channelFuture.channel();
            if (notTimeout) {
                if (clientChannel != null && clientChannel.isActive()) {
                    log.info("netty client started !!! {} connect to server", clientChannel.localAddress());
                    return true;
                }
                Throwable cause = channelFuture.cause();
                if (cause != null) {
                    exceptionHandler(cause);
                }
            } else {
                log.warn("connect remote host[{}] timeout {}s", clientChannel.remoteAddress(), 30);
            }
        } catch (Exception e) {
            exceptionHandler(e);
        }
        clientChannel.close();
        return false;
    }


    private void exceptionHandler(Throwable cause) {
        if (cause instanceof ConnectException) {
            log.error("connect error:{}", cause.getMessage());
        } else if (cause instanceof ClosedChannelException) {
            log.error("connect error:{}", "client has destroy");
        } else {
            log.error("connect error:", cause);
        }
    }

    public Channel getChannel() {
        return clientChannel;
    }
}

@Log4j2
class ClientHandler extends ChannelInboundHandlerAdapter {
    private final NettyClient nettyClient;

    private final TaskRunner taskRunner = SpringUtil.getApplicationContext().getBean(TaskRunner.class);

    private static final Map<String, Boolean> UNFINISHED_USER = new ConcurrentHashMap<>();

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
        if (UNFINISHED_USER.putIfAbsent(taskConfigDO.getDedeuserid(), true) == null) {
            BiliUserAndDays ret = taskRunner.run(taskConfigDO);
            if (ret != null) {
                BiliUser biliUser = ret.getBiliUser();
                int upgradeDays = ret.getUpgradeDays();
                BiliTaskUserDTO biliTaskUserDTO = new BiliTaskUserDTO();
                biliTaskUserDTO
                        .setDedeuserid(String.valueOf(biliUser.getMid()))
                        .setUsername(biliUser.getName())
                        .setLevel(biliUser.getLevel())
                        .setCoins(String.valueOf(biliUser.getCoins()))
                        .setCurrentExp(biliUser.getLevelExp().getCurrentExp())
                        .setDiffExp(biliUser.getLevelExp().getNextExp() - biliUser.getLevelExp().getCurrentExp())
                        .setUpgradeDays(upgradeDays)
                        .setVipStatus(biliUser.getVip().getStatus())
                        .setLastRunTime(LocalDateTime.now())
                        .setIsLogin(true);
                ctx.channel().writeAndFlush(Unpooled.copiedBuffer((JSONUtil.toJsonStr(biliTaskUserDTO) + "\n").getBytes(StandardCharsets.UTF_8)));
            }
            UNFINISHED_USER.remove(taskConfigDO.getDedeuserid());
        }
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
            log.error(cause);
        }
        ctx.pipeline().remove(this);
        ctx.channel().close();
        reconnection(ctx);
    }

    private void reconnection(ChannelHandlerContext ctx) {
        log.info("The client will reconnect after 3 seconds");
        initScheduledExecutor();

        scheduledExecutor.schedule(() -> {
            log.debug(">>> Reconnecting to the server. <<<");
            boolean connected = nettyClient.connect();
            if (connected) {
                log.info("Netty server connected.");
            } else {
                reconnection(ctx);
            }
        }, 3, TimeUnit.SECONDS);
    }
}
