package info.robotbrain.apoapsis;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class ApoapsisTokenHandler extends ChannelHandlerAdapter
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
            ctx.writeAndFlush("rx:ok");
            ctx.pipeline().replace(this, "apoapsis", new ApoapsisServerHandler());
            return;
        }
        ctx.writeAndFlush("rx:err:badtoken");
    }
}
