package com.xiaowu.server.handler;

import com.xiaowu.message.GroupMembersRequestMessage;
import com.xiaowu.message.GroupMembersResponseMessage;
import com.xiaowu.server.session.GroupSessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Set;

@ChannelHandler.Sharable
public class GroupMembersMessageSimpleChannelInboundHandler extends SimpleChannelInboundHandler<GroupMembersRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupMembersRequestMessage msg) throws Exception {
        GroupMembersRequestMessage requestMessage = (GroupMembersRequestMessage) msg;
        String groupName = requestMessage.getGroupName();
        Set<String> members = GroupSessionFactory.getGroupSession().getMembers(groupName);
        ctx.writeAndFlush(new GroupMembersResponseMessage(members));
    }
}
