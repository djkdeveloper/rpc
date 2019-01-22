package com.djk.djkrpc.server;

import com.djk.djkrpc.utils.RpcRequest;
import com.djk.djkrpc.utils.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dujinkai on 2019/1/3.
 * 服务端处理器
 */
@Slf4j
public class RpcServerHandle extends SimpleChannelInboundHandler<RpcRequest> {

    /**
     * rpc里面的实体 bean是接口名称 value是接口的实现类
     */
    private Map<String, Object> rpcBeans;

    /**
     * 固定线程池
     */
    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    public RpcServerHandle(Map<String, Object> rpcBeans) {
        this.rpcBeans = rpcBeans;
    }

    /**
     * 接收到客户端的请求
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        log.debug("receive request: {}", rpcRequest);
        executorService.submit(() -> {
            RpcResponse response = new RpcResponse(rpcRequest.getRequestId());
            try {
                response.setResult(handle(rpcRequest));
            } catch (Throwable t) {
                response.setError(t.toString());
                log.error("rpc server fail...", t);
            }

            channelHandlerContext.writeAndFlush(response);
        });
    }

    /**
     * 通过反射调用服务端的接口方法
     *
     * @param request 请求实体
     * @return 返回
     * @throws Throwable
     */
    private Object handle(RpcRequest request) throws Throwable {
        String className = request.getInterfaceName();
        Object serviceBean = rpcBeans.get(className);

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }


}
