package com.djk.djkrpc.server;

import com.djk.djkrpc.utils.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dujinkai on 2019/1/3.
 * rpc服务端
 */
@Slf4j
public class RpcServer implements ApplicationContextAware, InitializingBean {

    public RpcServer(String serverAddress, String connectionString) {
        this.serverAddress = serverAddress;
        this.connectionString = connectionString;
    }

    /**
     * zk链接地址
     */
    private String connectionString;

    /**
     * 服务端地址
     */
    private String serverAddress;

    /**
     * rpc里面的实体 bean是接口名称 value是接口的实现类
     */
    private Map<String, Object> rpcBeans = new HashMap<>();

    /**
     * 获得所有标注为rpc 接口的实现类
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        // 获得所有注解有RpcService的类
        Map<String, Object> rpcBeans = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isNotEmpty(rpcBeans)) {
            for (Object serviceBean : rpcBeans.values()) {
                String interfaceName = serviceBean.getClass().getInterfaces()[0].getName();
                this.rpcBeans.put(interfaceName, serviceBean);
                log.debug("Loading service: {}", interfaceName);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 启动netty
        NettyServer.start(rpcBeans, connectionString, serverAddress);
    }
}
