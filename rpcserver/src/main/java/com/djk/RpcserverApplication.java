package com.djk;

import com.djk.djkrpc.server.RpcServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RpcserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpcserverApplication.class, args);
    }

    @Bean
    public RpcServer rpcServer() {
        return new RpcServer("127.0.0.1:8888", "127.0.0.1:2181");
    }

}

