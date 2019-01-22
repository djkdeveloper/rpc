package com.djk.djkrpc.register;

import com.djk.djkrpc.utils.ZookeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.util.Objects;

/**
 * Created by dujinkai on 2019/1/3.
 * zk注册服务
 */
@Slf4j
public class RegisterServer {

    private static RegisterServer REGISTERSERVER = new RegisterServer();

    private RegisterServer() {
    }

    public static RegisterServer getInstance() {
        return REGISTERSERVER;
    }

    /**
     * zk客户端
     */
    private CuratorFramework curatorFramework;

    /**
     * 初始化zk客户端
     *
     * @param connectionString 链接对象
     */
    public void initZk(String connectionString) {
        log.debug("begin to initZk and connectionString:{}", connectionString);
        curatorFramework = ZookeeperUtils.createSimpleZkClient(connectionString);
        curatorFramework.start();
    }


    /**
     * 注册zk
     *
     * @param connectionString 链接信息
     */
    public void register(String connectionString, String serverAddress) {
        log.debug("register zk and connectionString:{} \r\n serverAddress:{}", connectionString, serverAddress);
        if (Objects.isNull(curatorFramework)) {
            initZk(connectionString);
        }

        try {
            // 如果跟节点不存在则创建根结点
            if (!ZookeeperUtils.isNodeExist(curatorFramework, ZookeeperUtils.ROOT)) {
                ZookeeperUtils.create(curatorFramework, ZookeeperUtils.ROOT, new byte[0]);
            }

            // 创建自己的临时节点
            ZookeeperUtils.createEphemeral(curatorFramework, ZookeeperUtils.ROOT + "/server", serverAddress.getBytes());

        } catch (Exception e) {
            log.error("register connectionString :{} fail...", connectionString, e);
        }

    }

}
