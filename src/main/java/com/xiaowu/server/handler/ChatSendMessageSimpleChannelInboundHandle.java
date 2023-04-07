package com.xiaowu.server.handler;

import com.xiaowu.message.ChatRequestMessage;
import com.xiaowu.message.ChatResponseMessage;
import com.xiaowu.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 单发消息handler
 */
@ChannelHandler.Sharable
public class ChatSendMessageSimpleChannelInboundHandle extends SimpleChannelInboundHandler<ChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage msg) throws Exception {
        ChatRequestMessage requestMessage = (ChatRequestMessage) msg;
        String to = requestMessage.getTo();
        String content = requestMessage.getContent();
        Channel channel = SessionFactory.getSession().getChannel(to);
        if (channel == null) {
            ctx.writeAndFlush(new ChatResponseMessage(false, "用户:" + to + "，暂且不在线!"));
        } else {
            channel.writeAndFlush(new ChatResponseMessage(requestMessage.getFrom(), content));
        }

    }
}
