package com.xiaowu.client;

import com.xiaowu.message.LoginRequestMessage;
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

@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        startClient();
    }

    private static void startClient() {
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
                                    .addLast(loggingHandler)
                                    .addLast(messageCodecSharable)
                                    .addLast("client handler", new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                            log.debug("msg:{}", msg);
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
                                                System.out.println("等待输入。。。。");
                                                try {
                                                    System.in.read();
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
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
