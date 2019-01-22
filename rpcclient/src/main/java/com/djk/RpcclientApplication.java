package com.djk;

import com.djk.djkrpc.client.RpcClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RpcclientApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpcclientApplication.class, args);
    }

    @Bean
    public RpcClient rpcClient() {
        return new RpcClient("127.0.0.1:2181");
    }

}

