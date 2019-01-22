package com.djk.rpcclient;

import com.djk.api.HelloService;
import com.djk.djkrpc.utils.RpcClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by dujinkai on 2019/1/22.
 */
@RestController
public class HelloController {

    @RpcClient
    private HelloService helloService;

    @RequestMapping("/test")
    public String test(String name) {
        return helloService.sayHello(name);
    }


}
