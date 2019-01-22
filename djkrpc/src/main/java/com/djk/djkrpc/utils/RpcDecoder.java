package com.djk.djkrpc.utils;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by dujinkai on 2018/10/28.
 * 协议解码
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private Class clazs;

    public RpcDecoder(Class clazs) {
        this.clazs = clazs;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        Object obj = JSON.parseObject(data, clazs);
        list.add(obj);
    }
}
