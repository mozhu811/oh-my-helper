package io.cruii.handler;

import cn.hutool.json.JSONUtil;
import io.cruii.pojo.po.TaskConfig;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        log.info("Receive message from client [{}] : {}", ctx.channel().remoteAddress(), msg);
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
        String jsonConfig = JSONUtil.toJsonStr(msg)  + "\n";
        byte[] taskConfig = jsonConfig.getBytes();
        log.debug(">>> Sending message, dedeuserid: {} <<<", ((TaskConfig) msg).getDedeuserid());
        CHANNELS.forEach(c -> c.writeAndFlush(Unpooled.copiedBuffer(taskConfig)));
    }
}
