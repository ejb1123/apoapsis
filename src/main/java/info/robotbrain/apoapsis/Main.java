package info.robotbrain.apoapsis;

import info.robotbrain.apoapsis.internal.TextWebSocketCodec;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Main
{

    public static void main(String[] args) throws IOException,
            InterruptedException
    {
        ServerOrm.init();
        Properties cfg = new Properties();
        if (new File("apoapsis.properties").exists()) {
            cfg.load(new FileReader("apoapsis.properties"));
        }
        int port = Integer.parseInt(cfg.getProperty("port", "25564"));
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>()
                    {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception
                        {
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(65536));

                            ch.pipeline().addLast(new WebSocketServerProtocolHandler("/apoapsis"));
                            ch.pipeline().addLast(new WebSocketFrameAggregator(65536));
                            ch.pipeline().addLast(new TextWebSocketCodec());

                            ch.pipeline().addLast("apoapsisauth", new ApoapsisTokenHandler(cfg.getProperty("token", "UNDEFINED")));
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync();

            //TODO: what goes here?!?!

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
