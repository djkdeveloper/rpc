package com.djk.djkrpc.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by dujinkai on 2019/1/3.
 * rpc 客户端注解  该注解放在接口上表面这个接口是rpc客户端的接口 会生成一个rpc的代理对象
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcClient {
}
