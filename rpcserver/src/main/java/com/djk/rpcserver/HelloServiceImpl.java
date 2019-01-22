package com.djk.rpcserver;

import com.djk.api.HelloService;
import com.djk.djkrpc.utils.RpcService;

/**
 * Created by dujinkai on 2019/1/22.
 */
@RpcService
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
        return "hepp word" + name;
    }
}
