package com.djk.djkrpc.client;

import com.djk.djkrpc.utils.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by dujinkai on 2019/1/22.
 * rpc代理 生成客户端的代理类
 */
@Slf4j
public class RpcProxy {

    /**
     * 获得接口的代理类
     *
     * @param iface 接口
     * @return 返回接口的代理类
     */
    public static Object getObject(Class<?> iface) {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{iface}, (proxy, method, args) -> {
            RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setRequestId(UUID.randomUUID().toString());
            rpcRequest.setInterfaceName(method.getDeclaringClass().getName());
            rpcRequest.setMethodName(method.getName());
            rpcRequest.setParameterTypes(method.getParameterTypes());
            rpcRequest.setParameters(args);

            log.debug("begin to send request ...and rpcRequest:{}", rpcRequest);
            // 发送请求到服务端 等待结果
            return RpcConnetManager.getInstance().chooseHandler().sendRequest(rpcRequest).getResponse().getResult();
        });
    }
}
