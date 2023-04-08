package com.xiaowu.client;

import com.xiaowu.message.RpcRequestMessage;
import com.xiaowu.protocol.MessageCodecSharable;
import com.xiaowu.protocol.ProcotolFrameDecoder;
import com.xiaowu.server.handler.RpcRequestMessageSimpleChannelInboundHandle;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcClient {
    public static void main(String[] args) {
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        LoggingHandler loggingHandler = new LoggingHandler();
        MessageCodecSharable messageCodecSharable = new MessageCodecSharable();
//        RpcRequestMessageSimpleChannelInboundHandle rpcRequestMessageSimpleChannelInboundHandle = new RpcRequestMessageSimpleChannelInboundHandle();
        try {
            Channel channel = new Bootstrap()
                    .channel(NioSocketChannel.class)
                    .group(workGroup)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProcotolFrameDecoder());
                            ch.pipeline().addLast(loggingHandler);
                            ch.pipeline().addLast(messageCodecSharable);
//                            ch.pipeline().addLast(rpcRequestMessageSimpleChannelInboundHandle);
                        }
                    })
                    .connect("127.0.0.1", 8080)
                    .sync()
                    .channel();
            channel.writeAndFlush(new RpcRequestMessage(
                    1,
                    "com.xiaowu.rpc.HelloWorldService",
                    "sayHello",
                    String.class,
                    new Class[]{String.class},
                    new Object[]{"xiao.wu"}
            )).addListener(promise -> {
                if (!promise.isSuccess()) {
                    Throwable cause = promise.cause();
                    log.error("error", cause);
                }
            });
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            workGroup.shutdownGracefully();
        }
    }
}
