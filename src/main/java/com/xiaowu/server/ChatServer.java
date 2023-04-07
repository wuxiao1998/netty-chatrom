package com.xiaowu.server;

import com.xiaowu.message.LoginRequestMessage;
import com.xiaowu.message.LoginResponseMessage;
import com.xiaowu.protocol.MessageCodecSharable;
import com.xiaowu.protocol.ProcotolFrameDecoder;
import com.xiaowu.server.service.UserService;
import com.xiaowu.server.service.UserServiceFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatServer {
    public static void main(String[] args) {
        startServer();
    }

    private static void startServer() {
        log.info("start netty server....");
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        LoggingHandler loggingHandler = new LoggingHandler();
        MessageCodecSharable messageCodecSharable = new MessageCodecSharable();
        try {
            Channel channel = new ServerBootstrap().channel(NioServerSocketChannel.class).group(new NioEventLoopGroup()).childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    // 日志打印handler
                    ch.pipeline()
                            // 黏包半包处理器
                            .addLast(new ProcotolFrameDecoder()).addLast(loggingHandler)
                            // 自定义编解码处理器
                            .addLast(messageCodecSharable)
                            .addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    super.channelActive(ctx);
                                    log.info("channel {} is connection...", ctx.channel().remoteAddress());
                                }
                            })
                            .addLast(new SimpleChannelInboundHandler<LoginRequestMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage msg) throws Exception {
                                    String username = msg.getUsername();
                                    String password = msg.getPassword();
                                    UserService userService = UserServiceFactory.getUserService();
                                    boolean login = userService.login(username, password);
                                    LoginResponseMessage responseMessage = new LoginResponseMessage(false, "登录失败");
                                    if (login) {
                                        responseMessage = new LoginResponseMessage(true, "登录成功");
                                    }
                                    ctx.writeAndFlush(responseMessage);
                                }
                            })
                    ;
                }
            }).bind("127.0.0.1", 8080).sync().channel();
            channel.closeFuture().sync();
        } catch (Throwable e) {
            log.error("server error ", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
