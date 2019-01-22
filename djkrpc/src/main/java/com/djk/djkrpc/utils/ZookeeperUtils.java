package com.djk.djkrpc.utils;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by dujinkai on 2019/1/3.
 * zookeeper 客户端
 */
public class ZookeeperUtils {

    /**
     * zk的根节点
     */
    public static final String ROOT = "/Register";

    /**
     * 私有构造器防止初始化
     */
    private ZookeeperUtils() {

    }

    /**
     * 创建简易的zk实例
     *
     * @param connectionString 链接信息 比如127.0.0.1:2181
     * @return 返回CuratorFramework实例
     */
    public static CuratorFramework createSimpleZkClient(String connectionString) {
        return CuratorFrameworkFactory.newClient(connectionString,
                new ExponentialBackoffRetry(1000, 3));
    }


    /**
     * 创建自定义的zk实例
     *
     * @param connectionString    链接信息 比如127.0.0.1:2181
     * @param retryPolicy         重试策略
     * @param connectionTimeoutMs 链接超时时间
     * @param sessionTimeoutMs    session超时时间
     * @return 返回CuratorFramework实例
     */
    public static CuratorFramework createWithOptions(String connectionString,
                                                     RetryPolicy retryPolicy, int connectionTimeoutMs,
                                                     int sessionTimeoutMs) {

        return CuratorFrameworkFactory.builder()
                .connectString(connectionString).retryPolicy(retryPolicy)
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs).build();
    }


    /**
     * 判断节点是否存在
     *
     * @param client zk客户端
     * @param path   节点路径
     * @return 存在返回true  不存在返回false
     * @throws Exception
     */
    public static boolean isNodeExist(CuratorFramework client, String path) throws Exception {
        return client.checkExists().forPath(path) == null ? false : true;
    }


    /**
     * 创建节点 （固定节点）
     *
     * @param client  zk客户端
     * @param path    节点的路径
     * @param payload 节点下的数据
     * @throws Exception 抛出异常
     */
    public static void create(CuratorFramework client, String path,
                              byte[] payload) throws Exception {
        client.create().forPath(path, payload);
    }


    /**
     * 创建临时节点
     *
     * @param client  zk客户端
     * @param path    节点路径
     * @param payload 节点数据
     * @throws Exception
     */
    public static void createEphemeral(CuratorFramework client, String path,
                                       byte[] payload) throws Exception {
        client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, payload);
    }

    /**
     * 获得节点数据
     *
     * @param client 客户端
     * @param path   节点路径
     * @return 返回节点下的数据
     */
    public static String getNodeDate(CuratorFramework client, String path)
            throws Exception {
        return new String(client.getData().forPath(path));
    }

    /**
     * 获得指定路径下的所有子节点的数据
     *
     * @return 返回指定路径下的所有子节点的数据
     */
    public static List<String> getChildrenNodeData(CuratorFramework client, String path) throws Exception {
        List<String> children = client.getChildren().forPath(path);

        if (CollectionUtils.isEmpty(children)) {
            return Collections.emptyList();
        }

        return children.stream().map(child -> {
            try {
                return ZookeeperUtils.getNodeDate(client, ROOT + "/" + child);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }).collect(Collectors.toList());
    }
}

