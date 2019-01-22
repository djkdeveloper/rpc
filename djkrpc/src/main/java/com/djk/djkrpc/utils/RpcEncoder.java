package com.djk.djkrpc.utils;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by dujinkai on 2019/1/3.
 * rpc 解码
 */
public class RpcEncoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        // 对象转化成json
        String json = JSON.toJSONString(o);
        byteBuf.writeInt(json.length());
        byteBuf.writeBytes(json.getBytes());
    }
}
