package com.xiaowu.server.handler;

import com.xiaowu.message.GroupCreateRequestMessage;
import com.xiaowu.message.GroupCreateResponseMessage;
import com.xiaowu.server.session.Group;
import com.xiaowu.server.session.GroupSessionFactory;
import com.xiaowu.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Set;

@ChannelHandler.Sharable
public class GroupCreateMessageSimpleChannelInboundHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupCreateRequestMessage msg) throws Exception {
        GroupCreateRequestMessage requestMessage =   (GroupCreateRequestMessage)msg;
        String groupName = requestMessage.getGroupName();
        Set<String> members = requestMessage.getMembers();
        Group group = GroupSessionFactory.getGroupSession().createGroup(groupName, members);
        if(group == null){
            for (String member : members) {
                Channel channel = SessionFactory.getSession().getChannel(member);
                if(channel != null){
                    channel.writeAndFlush(new GroupCreateResponseMessage(true,"您加入"+groupName+"群聊"));
                }
            }
        }else{
            ctx.writeAndFlush("创建群失败，群名已存在！");
        }
    }
}
