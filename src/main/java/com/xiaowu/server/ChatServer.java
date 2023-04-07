package com.xiaowu.server;

import com.xiaowu.protocol.MessageCodecSharable;
import com.xiaowu.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatServer {
    public static void main(String[] args) throws InterruptedException {
        startServer();
    }

    private static void startServer() {
        log.info("start netty server....");
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        LoggingHandler loggingHandler = new LoggingHandler();
        MessageCodecSharable messageCodecSharable = new MessageCodecSharable();
        try {
            Channel channel = new ServerBootstrap()
                    .channel(NioServerSocketChannel.class)
                    .group(new NioEventLoopGroup())
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            // 日志打印handler
                            ch.pipeline().addLast(loggingHandler)
                                    // 黏包半包处理器
                                    .addLast(new ProcotolFrameDecoder())
                                    // 自定义编解码处理器
                                    .addLast(messageCodecSharable);
                        }
                    })
                    .bind("127.0.0.1", 8080).sync().channel();
            channel.closeFuture().sync();
        } catch (Throwable e) {
            log.error("error is ", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
