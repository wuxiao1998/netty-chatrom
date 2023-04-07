package com.xiaowu.server.handler;

import com.xiaowu.message.GroupJoinRequestMessage;
import com.xiaowu.message.GroupJoinResponseMessage;
import com.xiaowu.message.GroupMembersResponseMessage;
import com.xiaowu.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

@ChannelHandler.Sharable
public class GroupJoinMessageSimpleChannelInboundHandler extends SimpleChannelInboundHandler<GroupJoinRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupJoinRequestMessage msg) throws Exception {
        GroupJoinRequestMessage requestMessage = (GroupJoinRequestMessage) msg;
        String groupName = requestMessage.getGroupName();
        String username = requestMessage.getUsername();
        GroupSessionFactory.getGroupSession().joinMember(groupName, username);
        List<Channel> membersChannel = GroupSessionFactory.getGroupSession().getMembersChannel(groupName);
        for (Channel channel : membersChannel) {
            channel.writeAndFlush(new GroupJoinResponseMessage(true, username + "加入了群聊"));
        }
    }
}
