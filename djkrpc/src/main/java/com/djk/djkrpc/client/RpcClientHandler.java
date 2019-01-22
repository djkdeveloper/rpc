package com.djk.djkrpc.client;

import com.djk.djkrpc.utils.RPCFuture;
import com.djk.djkrpc.utils.RpcRequest;
import com.djk.djkrpc.utils.RpcResponse;
import io.netty.channel.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by dujinkai on 2019/1/22.
 * rpc客户端处理类
 */
@Data
@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    /**
     * 通道
     */
    private volatile Channel channel;

    /**
     * 服务器地址
     */
    private String serverAddress;

    /**
     * 正在处理的
     */
    private ConcurrentHashMap<String, RPCFuture> pendingRPC = new ConcurrentHashMap<>();


    public RpcClientHandler(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        System.out.println("receive msg from server:" + rpcResponse);
        RPCFuture rpcFuture = pendingRPC.get(rpcResponse.getRequestId());
        if (Objects.nonNull(rpcFuture)) {
            pendingRPC.remove(rpcResponse.getRequestId());
            rpcFuture.done(rpcResponse);
        }
    }

    /**
     * 发送请求
     *
     * @param request 请求
     * @return 返回阻塞结果
     */
    public RPCFuture sendRequest(RpcRequest request) {
        log.debug("sendRequest and request:{}", request);
        final CountDownLatch latch = new CountDownLatch(1);
        RPCFuture rpcFuture = new RPCFuture(request);
        pendingRPC.put(request.getRequestId(), rpcFuture);
        channel.writeAndFlush(request).addListener(future -> latch.countDown());
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }

        return rpcFuture;
    }
}
