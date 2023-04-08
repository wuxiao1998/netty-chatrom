package com.xiaowu.server.handler;

import com.xiaowu.message.RpcRequestMessage;
import com.xiaowu.message.RpcResponseMessage;
import com.xiaowu.server.service.ServicesFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@ChannelHandler.Sharable
@Slf4j
public class RpcRequestMessageSimpleChannelInboundHandle extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage msg) {
        RpcResponseMessage rpcResponseMessage = new RpcResponseMessage();
        try {
            RpcRequestMessage requestMessage = (RpcRequestMessage) msg;
            String interfaceName = requestMessage.getInterfaceName();
            String methodName = requestMessage.getMethodName();
            Class[] parameterTypes = requestMessage.getParameterTypes();
            Object[] parameterValue = requestMessage.getParameterValue();
            // 反射根据全类名获取服务类
            Object service = ServicesFactory.getService(Class.forName(interfaceName));
            Method method = service.getClass().getDeclaredMethod(methodName, parameterTypes);
            Object returnValue = method.invoke(service,parameterValue);
            rpcResponseMessage.setReturnValue(returnValue);
        } catch (Exception e) {
            log.error("rpc error", e);
            rpcResponseMessage.setExceptionValue(e);
        }
        ctx.writeAndFlush(rpcResponseMessage);
    }
}
