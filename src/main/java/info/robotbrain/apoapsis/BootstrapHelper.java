package info.robotbrain.apoapsis;

import java.util.List;
import java.util.function.Function;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

import info.robotbrain.apoapsis.internal.NewlineHandler;
import info.robotbrain.apoapsis.internal.TextWebSocketCodec;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

public class BootstrapHelper
{
	private static EventLoopGroup bossGroup = new NioEventLoopGroup();
	private static EventLoopGroup workerGroup = new NioEventLoopGroup();

	public static ServerBootstrap createWebSocket(SslContext context,
			String token)
	{

		return create(context, token, new HttpServerCodec(),
				new HttpObjectAggregator(65536),
				new WebSocketServerProtocolHandler("/"),
				new WebSocketFrameAggregator(65536), new TextWebSocketCodec());
	}

	private static ServerBootstrap create(SslContext context, String token,
			ChannelHandler... handlers)
	{
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.INFO))
				.childHandler(new ChannelInitializer<SocketChannel>()
				{
					@Override
					public void initChannel(SocketChannel ch) throws Exception
					{
						if (context != null) {
							ch.pipeline().addLast("ssl",
									context.newHandler(ch.alloc()));
						}
						ch.pipeline().addLast(handlers);
						ch.pipeline().addLast("apoapsisauth",
								new ApoapsisTokenHandler(token));
					}
				}).option(ChannelOption.SO_BACKLOG, 128)
				.childOption(ChannelOption.SO_KEEPALIVE, true);
		return b;
	}

	public static void close()
	{
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}

	public static ServerBootstrap createTcp(SslContext context, String token)
	{
		return create(context, token, new LineBasedFrameDecoder(65536), new StringDecoder(Charsets.UTF_8), new StringEncoder(Charsets.UTF_8), new NewlineHandler());
	}

}
