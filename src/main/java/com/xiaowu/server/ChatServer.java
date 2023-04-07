package com.xiaowu.server;

import com.xiaowu.protocol.MessageCodecSharable;
import com.xiaowu.protocol.ProcotolFrameDecoder;
import com.xiaowu.server.handler.*;
import com.xiaowu.server.session.GroupSessionFactory;
import com.xiaowu.server.session.SessionFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
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
        ChatSendMessageSimpleChannelInboundHandle chatSendMessageHandler = new ChatSendMessageSimpleChannelInboundHandle();
        LoginRequestMessageSimpleChannelInboundHandler loginRequestMessageSimpleChannelInboundHandler = new LoginRequestMessageSimpleChannelInboundHandler();
        GroupCreateMessageSimpleChannelInboundHandler groupCreateMessageSimpleChannelInboundHandler = new GroupCreateMessageSimpleChannelInboundHandler();
        GroupSendMessageSimpleChannelInboundHandler groupSendMessageSimpleChannelInboundHandler = new GroupSendMessageSimpleChannelInboundHandler();
        GroupMembersMessageSimpleChannelInboundHandler groupMembersMessageSimpleChannelInboundHandler = new GroupMembersMessageSimpleChannelInboundHandler();
        GroupJoinMessageSimpleChannelInboundHandler groupJoinMessageSimpleChannelInboundHandler = new GroupJoinMessageSimpleChannelInboundHandler();
        GroupQuitMessageSimpleChannelInboundHandler groupQuitMessageSimpleChannelInboundHandler = new GroupQuitMessageSimpleChannelInboundHandler();
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
                            .addLast(new IdleStateHandler(60, 0, 0))
                            .addLast(new ChannelDuplexHandler() {
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    if (evt instanceof IdleStateEvent) {
                                        IdleStateEvent event = (IdleStateEvent) evt;
                                        if (event.state() == IdleState.READER_IDLE) {
                                            SessionFactory.getSession().unbind(ctx.channel());
                                            ctx.channel().close();
                                        }
                                    }
                                }
                            })
                            .addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    super.channelActive(ctx);
                                    log.info("channel {} is connection...", ctx.channel().remoteAddress());
                                }

                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    log.info("{} incative", ctx.channel());
                                    SessionFactory.getSession().unbind(ctx.channel());
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    log.info("error:{}", cause.getMessage());
                                    SessionFactory.getSession().unbind(ctx.channel());
                                }
                            })
                            .addLast(loginRequestMessageSimpleChannelInboundHandler)
                            .addLast(chatSendMessageHandler)
                            .addLast(groupCreateMessageSimpleChannelInboundHandler)
                            .addLast(groupSendMessageSimpleChannelInboundHandler)
                            .addLast(groupMembersMessageSimpleChannelInboundHandler)
                            .addLast(groupJoinMessageSimpleChannelInboundHandler)
                            .addLast(groupQuitMessageSimpleChannelInboundHandler)


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
