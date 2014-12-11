package info.robotbrain.apoapsis.internal;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.List;

public class TextWebSocketCodec extends MessageToMessageCodec<WebSocketFrame, String>
{
    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) throws Exception
    {
        if (msg instanceof TextWebSocketFrame) {
            String text = ((TextWebSocketFrame) msg).text().trim();
            out.add(text);
        } else {
            throw new Error();
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception
    {
        out.add(new TextWebSocketFrame(msg));
    }
}
