package info.robotbrain.apoapsis.internal;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class NewlineHandler extends MessageToMessageEncoder<String>
{
    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception
    {
        if(!msg.endsWith("\n")) {
            msg += "\n";
        }
        out.add(msg);
    }
}
