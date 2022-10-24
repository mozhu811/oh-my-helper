package io.cruii.execution.component;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import io.cruii.execution.config.NettyConfiguration;
import io.cruii.pojo.po.TaskConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
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
                        socketChannel.pipeline().addLast("lineBasedFrameDecoder", new LineBasedFrameDecoder(1024));
                        socketChannel.pipeline().addLast("stringDecoder", new StringDecoder());
                        socketChannel.pipeline().addLast("clientHandler", new ClientHandler(NettyClient.this));
                        socketChannel.pipeline().addLast("idleState", new IdleStateHandler(0, 0, 5));
                    }
                });
        // 绑定端口并同步等待
        ChannelFuture channelFuture = bootstrap.connect(nettyConfig.getHost(), nettyConfig.getPort());
        channelFuture.addListener(new ConnectionListener(this));
    }
}

@Slf4j
class ConnectionListener implements ChannelFutureListener {
    private final NettyClient nettyClient;

    public ConnectionListener(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (!channelFuture.isSuccess()) {
            log.debug("Reconnecting");
            final EventLoop loop = channelFuture.channel().eventLoop();
            loop.schedule(() -> {
                nettyClient.doConnect(new Bootstrap(), loop);
            }, 5L, TimeUnit.SECONDS);
        }
    }
}

@Log4j2
class ClientHandler extends ChannelInboundHandlerAdapter{
    private final NettyClient nettyClient;

    TaskRunner taskRunner = SpringUtil.getApplicationContext().getBean(TaskRunner.class);

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
        TaskConfig taskConfig = JSONUtil.toBean(((String) msg), TaskConfig.class);
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
