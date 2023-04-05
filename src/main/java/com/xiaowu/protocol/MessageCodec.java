package com.xiaowu.protocol;

import com.xiaowu.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * 自定义协议
 * 解码和编码
 */
public class MessageCodec extends ByteToMessageCodec<Message> {
    private final static String MAGIC_NUM = "CHATROM";
    private final static byte Version = 1;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf byteBuf) throws Exception {
        // 1.魔数 7字节
        byteBuf.writeBytes(MAGIC_NUM.getBytes());
        // 2.协议版本 1
        byteBuf.writeByte(Version);
        // 3.序列化算法 0:jdk 1:json 1
        byteBuf.writeByte(0);
        // 4.指令类型  1
        byteBuf.writeByte(message.getMessageType());
        // 5.序列号 4
        byteBuf.writeInt(message.getSequenceId());
        // 6.获取内容的字节数组
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(message);
        byte[] bytes = bos.toByteArray();
        // 7.长度
        byteBuf.writeInt(bytes.length);
        // 8.写入内容
        byteBuf.writeBytes(bytes);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

    }
}
