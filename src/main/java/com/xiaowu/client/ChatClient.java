package com.xiaowu.client;

import com.xiaowu.message.*;
import com.xiaowu.protocol.MessageCodecSharable;
import com.xiaowu.protocol.ProcotolFrameDecoder;
import com.xiaowu.server.session.SessionFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        startClient();
    }

    private static void startClient() {
        CountDownLatch WAIT_FOR_LOGIN = new CountDownLatch(1);
        AtomicBoolean LOGIN = new AtomicBoolean(false);
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler loggingHandler = new LoggingHandler();
        MessageCodecSharable messageCodecSharable = new MessageCodecSharable();
        try {
            Channel channel = new Bootstrap()
                    .channel(NioSocketChannel.class)
                    .group(group)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new ProcotolFrameDecoder())
                                    .addLast(messageCodecSharable)
                                    .addLast(new IdleStateHandler(0, 3, 0))
                                    .addLast(new ChannelDuplexHandler() {
                                        @Override
                                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                            if (evt instanceof IdleStateEvent) {
                                                IdleStateEvent event = (IdleStateEvent) evt;
                                                if (event.state() == IdleState.WRITER_IDLE) {
//                                                    log.debug("send heart...");
                                                    ctx.writeAndFlush(new PingMessage());
                                                }
                                            }
                                        }
                                    })
                                    .addLast("client handler", new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                            log.info("{} incative", ctx.channel());
                                        }

                                        @Override
                                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                            log.info("error:{}", cause.getMessage());
                                        }

                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                            log.debug("msg:{}", msg);
                                            if (msg instanceof LoginResponseMessage) {
                                                LoginResponseMessage loginResponseMessage = (LoginResponseMessage) msg;
                                                LOGIN.set(loginResponseMessage.isSuccess());
                                            }
                                            WAIT_FOR_LOGIN.countDown();
                                        }

                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                            // 负责接受用户在控制台的输入,负责向服务器发送各种消息
                                            new Thread(() -> {
                                                Scanner scanner = new Scanner(System.in);
                                                System.out.println("请输入用户名:");
                                                String username = scanner.nextLine();
                                                System.out.println("请输入密码:");
                                                String password = scanner.nextLine();
                                                LoginRequestMessage message = new LoginRequestMessage(username, password);
                                                ctx.writeAndFlush(message);
                                                try {
                                                    WAIT_FOR_LOGIN.await();
                                                } catch (InterruptedException e) {
                                                    throw new RuntimeException(e);
                                                }
                                                if (!LOGIN.get()) {
                                                    ctx.channel().close();
                                                    return;
                                                }
                                                System.out.println("login success");
                                                while (true) {
                                                    System.out.println("==================================");
                                                    System.out.println("send [username] [content]");
                                                    System.out.println("gsend [group name] [content]");
                                                    System.out.println("gcreate [group name] [m1,m2,m3...]");
                                                    System.out.println("gmembers [group name]");
                                                    System.out.println("gjoin [group name]");
                                                    System.out.println("gquit [group name]");
                                                    System.out.println("quit");
                                                    System.out.println("==================================");
                                                    String command = scanner.nextLine();
                                                    String[] arrays = command.split(" ");
                                                    switch (arrays[0]) {
                                                        case "send":
                                                            ChatRequestMessage chatRequestMessage = new ChatRequestMessage(username, arrays[1], arrays[2]);
                                                            ctx.writeAndFlush(chatRequestMessage);
                                                            break;
                                                        case "gsend":
                                                            GroupChatRequestMessage groupChatRequestMessage = new GroupChatRequestMessage(username, arrays[1], arrays[2]);
                                                            ctx.writeAndFlush(groupChatRequestMessage);
                                                            break;
                                                        case "gcreate":
                                                            Set<String> members = Arrays.stream(arrays[2].split(",")).collect(Collectors.toSet());
                                                            members.add(username);
                                                            GroupCreateRequestMessage groupCreateRequestMessage = new GroupCreateRequestMessage(
                                                                    arrays[1], members
                                                            );
                                                            ctx.writeAndFlush(groupCreateRequestMessage);
                                                            break;
                                                        case "gmembers":
                                                            GroupMembersRequestMessage groupMembersRequestMessage = new GroupMembersRequestMessage(arrays[1]);
                                                            ctx.writeAndFlush(groupMembersRequestMessage);
                                                            break;
                                                        case "gjoin":
                                                            GroupJoinRequestMessage groupJoinRequestMessage = new GroupJoinRequestMessage(username, arrays[1]);
                                                            ctx.writeAndFlush(groupJoinRequestMessage);
                                                            break;
                                                        case "gquit":
                                                            GroupQuitRequestMessage groupQuitRequestMessage = new GroupQuitRequestMessage(username, arrays[1]);
                                                            ctx.writeAndFlush(groupQuitRequestMessage);
                                                            break;
                                                        case "quit":
                                                            ctx.channel().close();
                                                            return;
                                                    }

                                                }

                                            }, "system in").start();
                                        }
                                    });
                        }
                    })
                    .connect("127.0.0.1", 8080).sync().channel();
            channel.closeFuture().sync();
        } catch (Throwable e) {
            log.error("client error", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
