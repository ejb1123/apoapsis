package info.robotbrain.apoapsis;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

public class ApoapsisTokenHandler extends ChannelDuplexHandler
{

    private String token;

    public ApoapsisTokenHandler(String token)
    {
        this.token = token;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception
    {
        System.out.println(msg);
        String s = "token:" + token;
        System.out.println(s);
        if (s.equals(msg)) {
            ctx.writeAndFlush("rx:token");
            ctx.pipeline().replace(this, "apoapsis", new ApoapsisServerHandler());
            return;
        }
        ctx.writeAndFlush("rx:err:badtoken");
    }
}
