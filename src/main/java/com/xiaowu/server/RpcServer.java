package com.xiaowu.server;

import com.xiaowu.protocol.MessageCodecSharable;
import com.xiaowu.protocol.ProcotolFrameDecoder;
import com.xiaowu.server.handler.RpcRequestMessageSimpleChannelInboundHandle;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;

public class RpcServer {
    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        LoggingHandler loggingHandler = new LoggingHandler();
        MessageCodecSharable messageCodecSharable = new MessageCodecSharable();
        RpcRequestMessageSimpleChannelInboundHandle rpcRequestMessageSimpleChannelInboundHandle = new RpcRequestMessageSimpleChannelInboundHandle();
        try {
            Channel channel = new ServerBootstrap()
                    .channel(NioServerSocketChannel.class)
                    .group(bossGroup, workGroup)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProcotolFrameDecoder());
                            ch.pipeline().addLast(loggingHandler);
                            ch.pipeline().addLast(messageCodecSharable);
                            ch.pipeline().addLast(rpcRequestMessageSimpleChannelInboundHandle);
                        }
                    })
                    .bind()
                    .sync()
                    .channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }
}
