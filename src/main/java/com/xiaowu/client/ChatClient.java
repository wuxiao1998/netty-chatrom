package com.xiaowu.client;

import com.xiaowu.message.LoginRequestMessage;
import com.xiaowu.message.LoginResponseMessage;
import com.xiaowu.protocol.MessageCodecSharable;
import com.xiaowu.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

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
                                    .addLast("client handler", new ChannelInboundHandlerAdapter() {
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
