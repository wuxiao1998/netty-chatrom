package com.xiaowu.protocol;

import com.xiaowu.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * 自定义协议
 * 解码和编码
 */
@Slf4j
public class MessageCodec extends ByteToMessageCodec<Message> {
    private final static byte[] MAGIC_NUM = {1, 2, 3, 4};
    private final static byte Version = 1;

    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf byteBuf) throws Exception {
        // 1.魔数 4字节
        byteBuf.writeBytes(MAGIC_NUM);
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
    public void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 1.获取魔数
        ByteBuf magicNum = byteBuf.readBytes(MAGIC_NUM.length);
        // 2.获取协议版本
        byte version = byteBuf.readByte();
        // 3.获取序列化算法
        byte serializeType = byteBuf.readByte();
        // 4.获取指令类型
        byte messageType = byteBuf.readByte();
        // 5.获取序列号
        int sequenceId = byteBuf.readInt();
        // 6.获取长度
        int length = byteBuf.readInt();
        // 7.获取内容
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes, 0, length);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Message message = (Message) ois.readObject();
        log.debug(
                "magicNum:{},version:{},serializeType:{},messageType:{},sequenceId:{},length:{}",
                magicNum, version, serializeType, messageType, sequenceId, length
        );
        log.debug("message is {}", message);
        // 放入list，传入下一个handler
        list.add(message);
    }
}
