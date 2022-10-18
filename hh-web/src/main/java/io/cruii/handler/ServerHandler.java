package io.cruii.handler;

import cn.hutool.json.JSONUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cruii
 * Created on 2022/10/17
 */
@Slf4j
@Component
public class ServerHandler extends ChannelInboundHandlerAdapter {
    private static final List<Channel> CHANNELS = new ArrayList<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("{} is online.", ctx.channel().remoteAddress());
        CHANNELS.add(ctx.channel());
    }

    /**
     * 客户端数据到来时触发
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        log.info("Receive message from client [{}] : {}", ctx.channel().remoteAddress(), buf.toString(StandardCharsets.UTF_8));
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
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        // 释放与ChannelHandlerContext相关联的资源
        ctx.close();
    }

    public void sendMsg(Object msg) {
        byte[] taskConfig = JSONUtil.toJsonStr(msg).getBytes();
        log.debug(">>> Sending message, the num of channel: {} <<<", CHANNELS.size());
        CHANNELS.forEach(c -> c.writeAndFlush(Unpooled.copiedBuffer(taskConfig)));
    }
}
