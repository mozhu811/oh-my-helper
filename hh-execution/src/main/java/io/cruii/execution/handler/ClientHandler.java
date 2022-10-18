package io.cruii.execution.handler;

import io.cruii.execution.component.NettyClient;
import io.cruii.execution.config.NettyConfiguration;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author cruii
 * Created on 2022/10/17
 */
@Slf4j
public class ClientHandler extends ChannelInboundHandlerAdapter {
    private final NettyClient nettyClient;

    public ClientHandler(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    /**
     * 连接到服务器时触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(Unpooled.copiedBuffer("current time", StandardCharsets.UTF_8));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.warn(">>> The server [{}] is disconnected. <<<", ctx.channel().remoteAddress());
        log.debug(">>> Starting reconnect to the server. <<<");
        ctx.channel().eventLoop().schedule(() -> {
            log.debug(">>> Reconnecting to the server. <<<");
            nettyClient.connect();
        }, 1, TimeUnit.SECONDS);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.debug(">>> Starting reconnect to the server. <<<");
        ctx.channel().eventLoop().schedule(() -> {
            log.debug(">>> Reconnecting to the server. <<<");
            nettyClient.connect();
        }, 1, TimeUnit.SECONDS);
        super.channelUnregistered(ctx);
    }

    /**
     * 消息到来时触发
     */
    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        log.info("Receive message: {} ", buf.toString(StandardCharsets.UTF_8));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
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
