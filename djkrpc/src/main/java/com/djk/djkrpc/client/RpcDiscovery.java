package com.djk.djkrpc.client;

import com.djk.djkrpc.utils.ZookeeperUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by dujinkai on 2019/1/22.
 * 客户端发现服务
 */
@Slf4j
@Data
public class RpcDiscovery {

    private static RpcDiscovery RPCDISCOVERY = new RpcDiscovery();

    /**
     * 服务器地址集合
     */
    private volatile Set<String> serverList = new HashSet<>();

    private RpcDiscovery() {
    }

    public static RpcDiscovery getInstance() {
        return RPCDISCOVERY;
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
     * 观察服务器
     *
     * @param connectionString zk链接字符串
     */
    public void watchServer(String connectionString) {
        log.debug("begin to watchServer and connectionString:{}", connectionString);
        if (Objects.isNull(curatorFramework)) {
            initZk(connectionString);
        }

        try {
            // 如果跟节点不存在则创建根结点
            if (!ZookeeperUtils.isNodeExist(curatorFramework, ZookeeperUtils.ROOT)) {
                ZookeeperUtils.create(curatorFramework, ZookeeperUtils.ROOT, new byte[0]);
            }

            // 获得注册节点下的所有子节点
            serverList.addAll(ZookeeperUtils.getChildrenNodeData(curatorFramework, ZookeeperUtils.ROOT));

            // 更新服务器
            updateServer();

            // 获得注册节点下的所有子节点
            PathChildrenCache childrenCache = new PathChildrenCache(curatorFramework,
                    ZookeeperUtils.ROOT, true);

            childrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);

            childrenCache.getListenable().addListener((client, event) -> {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        log.debug("server add...");
                        serverList.add(new String(event.getData().getData()));
                        updateServer();
                        break;
                    case CHILD_REMOVED:
                        log.debug("server down...");
                        serverList.remove(new String(event.getData().getData()));
                        updateServer();
                        break;
                    default:
                        break;
                }
            });
        } catch (Exception e) {
            log.error("watchServer fail...", e);
        }
    }

    /**
     * 更新服务器
     */
    private void updateServer() {
        log.info("updateServer and now server list is :{}", serverList);
        RpcConnetManager.getInstance().updateConnectedServer(this.serverList);
    }
}
