package com.xiaowu.server.handler;

import com.xiaowu.message.GroupJoinResponseMessage;
import com.xiaowu.message.GroupQuitRequestMessage;
import com.xiaowu.message.GroupQuitResponseMessage;
import com.xiaowu.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

@ChannelHandler.Sharable
public class GroupQuitMessageSimpleChannelInboundHandler extends SimpleChannelInboundHandler<GroupQuitRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupQuitRequestMessage msg) throws Exception {
        GroupQuitRequestMessage requestMessage = (GroupQuitRequestMessage) msg;
        String groupName = requestMessage.getGroupName();
        String username = requestMessage.getUsername();
        GroupSessionFactory.getGroupSession().removeMember(groupName, username);
        List<Channel> membersChannel = GroupSessionFactory.getGroupSession().getMembersChannel(groupName);
        for (Channel channel : membersChannel) {
            channel.writeAndFlush(new GroupQuitResponseMessage(true, username + "退出了群聊"));
        }
        ctx.writeAndFlush(new GroupQuitResponseMessage(true, "您已退出了群聊-" + groupName));
    }
}
