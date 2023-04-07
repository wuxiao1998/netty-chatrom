package com.xiaowu.test;


import com.xiaowu.message.LoginRequestMessage;
import com.xiaowu.protocol.MessageCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import org.junit.Test;

// jvm args: --add-opens java.base/jdk.internal.misc=ALL-UNNAMED -Dio.netty.tryReflectionSetAccessible=true
public class TestMessageCodec {

    @Test
    public void test1() throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(
                new LoggingHandler(),
                new LengthFieldBasedFrameDecoder(1024,11,4,0,0),
                new MessageCodec()
        );

        LoginRequestMessage loginRequestMessage = new LoginRequestMessage("zhangsan","1234567");

        // 测试出站点
        channel.writeOutbound(loginRequestMessage);
        // 测试入站
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null,loginRequestMessage,byteBuf);
        ByteBuf byte1 = byteBuf.slice(0, 100);
        ByteBuf byte2 = byteBuf.slice(100, byteBuf.readableBytes() - 100);
        byteBuf.retain();
        channel.writeInbound(byte1);
        channel.writeInbound(byte2);





    }
}
