package com.xiaowu.server.handler;

import com.xiaowu.message.GroupChatRequestMessage;
import com.xiaowu.message.GroupChatResponseMessage;
import com.xiaowu.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

@ChannelHandler.Sharable
public class GroupSendMessageSimpleChannelInboundHandler extends SimpleChannelInboundHandler<GroupChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupChatRequestMessage msg) throws Exception {
        GroupChatRequestMessage requestMessage = (GroupChatRequestMessage) msg;
        String content = requestMessage.getContent();
        String from = requestMessage.getFrom();
        String groupName = requestMessage.getGroupName();
        List<Channel> membersChannel = GroupSessionFactory.getGroupSession().getMembersChannel(groupName);
        for (Channel channel : membersChannel) {
            channel.writeAndFlush(new GroupChatResponseMessage(from, content));
        }
    }
}
