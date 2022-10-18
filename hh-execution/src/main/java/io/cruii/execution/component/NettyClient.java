package io.cruii.execution.component;

import io.cruii.execution.config.NettyConfiguration;
import io.cruii.execution.handler.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author cruii
 * Created on 2022/4/2
 */
@Slf4j
@Component
public class NettyClient implements CommandLineRunner {
    private final EventLoopGroup group = new NioEventLoopGroup();

    private final NettyConfiguration nettyConfig;

    public NettyClient(NettyConfiguration nettyConfig) {
        this.nettyConfig = nettyConfig;
    }

    private final Bootstrap bootstrap = new Bootstrap();

    @Override
    public void run(String... args) {
        new Thread(() -> {
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 自定义处理程序
                            socketChannel.pipeline().addLast(new ClientHandler(new NettyClient(nettyConfig)));
                        }
                    });
            connect();
        }).start();
    }

    public void connect(){
        // 绑定端口并同步等待
        ChannelFuture channelFuture = bootstrap.connect(nettyConfig.getHost(), nettyConfig.getPort());
        try {
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
