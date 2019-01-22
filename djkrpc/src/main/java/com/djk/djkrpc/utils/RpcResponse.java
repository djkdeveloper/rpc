package com.djk.djkrpc.utils;

import lombok.Data;

/**
 * Created by dujinkai on 2019/1/3.
 * rpc响应实体
 */
@Data
public class RpcResponse {

    public RpcResponse() {
    }

    public RpcResponse(String requestId) {
        this.requestId = requestId;
    }

    /**
     * 请求的id
     */
    private String requestId;

    /**
     * 结果
     */
    private Object result;

    /**
     * 错误信息
     */
    private String error;
}
