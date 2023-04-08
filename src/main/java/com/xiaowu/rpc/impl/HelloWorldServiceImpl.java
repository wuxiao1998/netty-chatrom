package com.xiaowu.rpc.impl;

import com.xiaowu.rpc.HelloWorldService;

public class HelloWorldServiceImpl implements HelloWorldService {
    @Override
    public String sayHello(String say) {
        return "hello world " + say;
    }
}
