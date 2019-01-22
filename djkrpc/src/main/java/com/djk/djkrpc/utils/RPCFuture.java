package com.djk.djkrpc.utils;

import lombok.Data;

import java.util.concurrent.CountDownLatch;

/**
 * Created by dujinkai on 2019/1/22.
 * RPC 通知
 */
@Data
public class RPCFuture {

    /**
     * 计数器
     */
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    /**
     * 请求
     */
    private RpcRequest request;

    /**
     * 响应
     */
    private RpcResponse response;

    public RPCFuture(RpcRequest request) {
        this.request = request;
    }

    /**
     * 完成
     *
     * @param rpcResponse 响应
     */
    public void done(RpcResponse rpcResponse) {
        this.response = rpcResponse;
        countDownLatch.countDown();
    }

    /**
     * 获得结果
     *
     * @return 返回结果 该方法会柱塞
     * @throws InterruptedException
     */
    public RpcResponse getResponse() {
        try {
            countDownLatch.await();
            return this.response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
