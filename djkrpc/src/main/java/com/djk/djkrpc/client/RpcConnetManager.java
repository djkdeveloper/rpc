package com.djk.djkrpc.client;

import com.djk.djkrpc.utils.RpcDecoder;
import com.djk.djkrpc.utils.RpcEncoder;
import com.djk.djkrpc.utils.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by dujinkai on 2019/1/22.
 * rpc 服务链接的管理
 */
@Slf4j
public class RpcConnetManager {

    private static RpcConnetManager RPCCONNETMANAGER = new RpcConnetManager();

    private RpcConnetManager() {
    }

    public static RpcConnetManager getInstance() {
        return RPCCONNETMANAGER;
    }


    /**
     * 可以使用的服务端链接
     */
    private CopyOnWriteArrayList<RpcClientHandler> connectedHandlers = new CopyOnWriteArrayList<>();

    /**
     * 服务端链接 映射map
     */
    private Map<String, RpcClientHandler> connectedServerNodes = new ConcurrentHashMap<>();


    /**
     * 固定线程池
     */
    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    /**
     * 计数
     */
    private AtomicInteger time = new AtomicInteger(0);


    /**
     * 更新服务器链接
     *
     * @param servers 服务器列表
     */
    public void updateConnectedServer(Set<String> servers) {
        log.debug("updateConnectedServer and servers:{}", servers);
        // 服务器列表为空 则删除所有服务器链接
        if (CollectionUtils.isEmpty(servers)) {
            connectedHandlers.clear();
            connectedServerNodes.clear();
        } else {
            // 判断出新增的服务器
            servers.stream().forEach(s -> {
                // 新增服务器
                if (!connectedServerNodes.keySet().contains(s)) {
                    connectServerNode(s);
                }
            });

            // 判断出删除的服务器
            connectedServerNodes.forEach((s, rpcClientHandler) -> {
                // 删除服务器
                if (!servers.contains(s)) {
                    connectedHandlers.remove(rpcClientHandler);
                    connectedServerNodes.remove(s);
                }
            });
        }
    }

    /**
     * 选择一个服务端的链接
     *
     * @return 返回服务端的链接
     */
    public RpcClientHandler chooseHandler() {

        if (CollectionUtils.isEmpty(connectedHandlers)) {
            return null;
        }

        return connectedHandlers.get((time.getAndAdd(1) + connectedHandlers.size()) % connectedHandlers.size());
    }

    /**
     * 链接服务器
     *
     * @param server 服务器地址
     */
    private void connectServerNode(String server) {
        executorService.submit(() -> {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(new NioEventLoopGroup()).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
                    socketChannel.pipeline().addLast(new RpcEncoder());
                    socketChannel.pipeline().addLast(new RpcDecoder(RpcResponse.class));
                    socketChannel.pipeline().addLast(new RpcClientHandler(server));
                }
            });

            String[] array = server.split(":");
            ChannelFuture channelFuture = bootstrap.connect(array[0], Integer.parseInt(array[1]));
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(final ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        log.debug("connetc server success and server:{}", server);
                        RpcClientHandler handler = channelFuture.channel().pipeline().get(RpcClientHandler.class);
                        addHandler(handler);
                    }
                }
            });

        });
    }


    /**
     * 加入服务端链接
     */
    private void addHandler(RpcClientHandler handler) {
        connectedHandlers.add(handler);
        connectedServerNodes.put(handler.getServerAddress(), handler);
    }
}
