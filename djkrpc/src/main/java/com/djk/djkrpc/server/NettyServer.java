package com.djk.djkrpc.server;

import com.djk.djkrpc.register.RegisterServer;
import com.djk.djkrpc.utils.RpcDecoder;
import com.djk.djkrpc.utils.RpcEncoder;
import com.djk.djkrpc.utils.RpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Created by dujinkai on 2019/1/3.
 * netty服务
 */
@Slf4j
public class NettyServer {

    /**
     * 接受请求的线程组
     */
    private static EventLoopGroup bossGroup = new NioEventLoopGroup();

    /**
     * 处理请求的线程组
     */
    private static EventLoopGroup workerGroup = new NioEventLoopGroup();

    /**
     * 启动服务
     */
    public static void start(Map<String, Object> rpcBeans, String connectionString, String serverAddress) {
        log.debug("begin to start NettyServer and connectionString:{} \r\n serverAddress:{}", connectionString, serverAddress);

        if (StringUtils.isEmpty(connectionString) || StringUtils.isEmpty(serverAddress)) {
            throw new IllegalArgumentException("connectionString or serverAddress is empty....");
        }

        ServerBootstrap b = new ServerBootstrap();
        try {
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).
                    option(ChannelOption.SO_BACKLOG, 1024).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
                    socketChannel.pipeline().addLast(new RpcEncoder());
                    socketChannel.pipeline().addLast(new RpcDecoder(RpcRequest.class));
                    socketChannel.pipeline().addLast(new RpcServerHandle(rpcBeans));
                }
            });
            String[] array = serverAddress.split(":");
            ChannelFuture cf = b.bind(array[0], Integer.parseInt(array[1])).sync();
            // 把自己作为一个服务节点注册到zk上
            RegisterServer.getInstance().register(connectionString, serverAddress);

            log.debug("rpc start success....");
            cf.channel().closeFuture().sync();
        } catch (InterruptedException i) {
            log.error("start NettyServer fail....", i);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
