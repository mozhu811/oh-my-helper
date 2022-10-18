package io.cruii.component;

import io.cruii.config.NettyConfiguration;
import io.cruii.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author cruii
 * Created on 2022/10/17
 */
@Component
@Slf4j
public class NettyServer implements CommandLineRunner {
    private final NettyConfiguration nettyConfiguration;
    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workGroup = new NioEventLoopGroup();

    public NettyServer(NettyConfiguration nettyConfiguration) {
        this.nettyConfiguration = nettyConfiguration;
    }

    public void start() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        // 自定义服务处理
                        socketChannel.pipeline().addLast("idleState", new IdleStateHandler(5, 0, 0));
                        socketChannel.pipeline().addLast("serverHandler", new ServerHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            ChannelFuture channelFuture = serverBootstrap.bind(nettyConfiguration.getHost(), nettyConfiguration.getPort()).sync();
            log.info("Netty server started on host/port: {}/{}", nettyConfiguration.getHost(), nettyConfiguration.getPort());
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            log.info("======Shutdown Netty Server Successful!=========");
        }
    }

    @Async
    @Override
    public void run(String... args) throws Exception {
        start();
    }
}
