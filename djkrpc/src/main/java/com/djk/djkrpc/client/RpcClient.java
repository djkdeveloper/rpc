package com.djk.djkrpc.client;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;

/**
 * Created by dujinkai on 2019/1/22.
 * RPC 客户端
 */
@Slf4j
@Data
public class RpcClient implements BeanPostProcessor, InitializingBean {

    /**
     * zk链接地址
     */
    private String connectionString;

    public RpcClient(String connectionString) {
        this.connectionString = connectionString;
    }

    /**
     * bean 初始化完成后设置bean中有@rpcclient 注解的属性 生成其代理
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            if (field.isAnnotationPresent(com.djk.djkrpc.utils.RpcClient.class)) {

                // 如果属性不是接口 则报错
                if (!field.getType().isInterface()) {
                    throw new RuntimeException("filed is not interface....");
                }

                field.setAccessible(true);
                // 设置属性的代理类
                field.set(bean, RpcProxy.getObject(field.getType()));
            }
        });

        return bean;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 客户端发现服务
        RpcDiscovery.getInstance().watchServer(connectionString);
    }
}
