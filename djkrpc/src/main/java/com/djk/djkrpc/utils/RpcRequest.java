package com.djk.djkrpc.utils;

import lombok.Data;

/**
 * Created by dujinkai on 2019/1/3.
 * rpc请求实体
 */
@Data
public class RpcRequest {

    /**
     * 请求id
     */
    private String requestId;

    /**
     * 请求的接口名称
     */
    private String interfaceName;

    /**
     * 请求的接口方法名称
     */
    private String methodName;

    /**
     * 请求接口方法的参数类型
     */
    private Class[] parameterTypes;

    /**
     * 请求接口方法的参数
     */
    private Object[] parameters;
}
