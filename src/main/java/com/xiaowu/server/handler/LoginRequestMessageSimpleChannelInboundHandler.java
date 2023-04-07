package com.xiaowu.server.handler;

import com.xiaowu.message.LoginRequestMessage;
import com.xiaowu.message.LoginResponseMessage;
import com.xiaowu.server.service.UserService;
import com.xiaowu.server.service.UserServiceFactory;
import com.xiaowu.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public
class LoginRequestMessageSimpleChannelInboundHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage msg) throws Exception {
        String username = msg.getUsername();
        String password = msg.getPassword();
        UserService userService = UserServiceFactory.getUserService();
        boolean login = userService.login(username, password);
        LoginResponseMessage responseMessage = new LoginResponseMessage(false, "登录失败");
        if (login) {
            // 保存用户channel信息
            SessionFactory.getSession().bind(ctx.channel(), username);
            responseMessage = new LoginResponseMessage(true, "登录成功");
        }
        ctx.writeAndFlush(responseMessage);
    }
}
