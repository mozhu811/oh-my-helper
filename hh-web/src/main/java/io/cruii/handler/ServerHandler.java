package io.cruii.handler;

import cn.hutool.json.JSONUtil;
import io.cruii.pojo.dto.BiliTaskUserDTO;
import io.cruii.pojo.entity.TaskConfigDO;
import io.cruii.service.BilibiliUserService;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cruii
 * Created on 2022/10/17
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {
    private static final List<Channel> CHANNELS = new ArrayList<>();

    private BilibiliUserService bilibiliUserService;

    @Autowired
    public void setBilibiliUserService(BilibiliUserService bilibiliUserService) {
        this.bilibiliUserService = bilibiliUserService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        SocketChannel channel = (SocketChannel) ctx.channel();
        log.debug("新客户端加入: {}", channel.localAddress().getHostString());
        CHANNELS.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        log.debug("客户端断开连接: {}", channel.localAddress().getHostString());
        CHANNELS.remove(ctx.channel());
    }

    /**
     * 客户端数据到来时触发
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug("Received message: {}", msg);
        if (JSONUtil.isTypeJSON(((String) msg))) {
            BiliTaskUserDTO biliTaskUserDTO = JSONUtil.toBean(((String) msg), BiliTaskUserDTO.class);
            bilibiliUserService.save(biliTaskUserDTO);
        }
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
        String jsonConfig = JSONUtil.toJsonStr(msg) + "\n";
        byte[] taskConfig = jsonConfig.getBytes();
        log.debug(">>> Sending message, dedeuserid: {} <<<", ((TaskConfigDO) msg).getDedeuserid());
        CHANNELS.forEach(c -> c.writeAndFlush(Unpooled.copiedBuffer(taskConfig)));
    }
}
