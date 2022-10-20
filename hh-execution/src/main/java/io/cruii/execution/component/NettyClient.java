package io.cruii.execution.component;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import io.cruii.execution.config.NettyConfiguration;
import io.cruii.pojo.po.TaskConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
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

    @Override
    public void run(String... args) {
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        doConnect(bootstrap, eventLoopGroup);
    }

    public void doConnect(Bootstrap bootstrap, EventLoopGroup eventLoopGroup) {
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        // 自定义处理程序
                        socketChannel.pipeline().addLast("idleState", new IdleStateHandler(0, 0, 5));
                        socketChannel.pipeline().addLast("clientHandler", new ClientHandler(NettyClient.this));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128);

        // 绑定端口并同步等待
        ChannelFuture channelFuture = bootstrap.connect(nettyConfig.getHost(), nettyConfig.getPort());
        channelFuture.addListener(new ConnectionListener(this));
    }
}

class ConnectionListener implements ChannelFutureListener {
    private final NettyClient nettyClient;

    public ConnectionListener(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (!channelFuture.isSuccess()) {
            System.out.println("Reconnect");
            final EventLoop loop = channelFuture.channel().eventLoop();
            loop.schedule(() -> {
                nettyClient.doConnect(new Bootstrap(), loop);
            }, 30L, TimeUnit.SECONDS);
        }
    }
}

@Slf4j
class ClientHandler extends ChannelInboundHandlerAdapter{
    private final NettyClient nettyClient;

    public ClientHandler(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    /**
     * 连接到服务器时触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello netty.", StandardCharsets.UTF_8));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.warn(">>> The server [{}] is disconnected. <<<", ctx.channel());
        log.debug(">>> Starting reconnect to the server. <<<");
        EventLoop eventLoop = ctx.channel().eventLoop();
        eventLoop.schedule(() -> {
            log.debug(">>> Reconnecting to the server. <<<");
            nettyClient.doConnect(new Bootstrap(), eventLoop);
        }, 30L, TimeUnit.SECONDS);
    }

    /**
     * 消息到来时触发
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        String jsonTaskConfig = buf.toString(StandardCharsets.UTF_8);
        TaskConfig taskConfig = JSONUtil.toBean(jsonTaskConfig, TaskConfig.class);
        TaskRunner taskRunner = SpringUtil.getApplicationContext().getBean(TaskRunner.class);
        taskRunner.run(taskConfig);
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
        cause.printStackTrace();
        ctx.close();
    }
}
