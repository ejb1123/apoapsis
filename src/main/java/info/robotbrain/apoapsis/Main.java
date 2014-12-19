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
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.Properties;

public class Main
{
    public static void main(String[] args) throws IOException, InterruptedException, CertificateException
    {
        ServerOrm.init();
        Properties cfg = new Properties();
        if (!new File("apoapsis.properties").exists()) {
            cfg.setProperty("port", "25564");
            cfg.setProperty("token", "UNDEFINED");
            cfg.setProperty("ssl", "false");
            cfg.setProperty("ssl.cert", "");
            cfg.setProperty("ssl.pk", "");
            try (FileWriter writer = new FileWriter("apoapsis.properties")) {
                cfg.store(writer, "Apoapsis Settings");
            }
        } else {
            cfg.load(new FileReader("apoapsis.properties"));
        }
        boolean ssl = "true".equals(cfg.getProperty("ssl", ""));
        final SslContext context;
        if (ssl) {
        	File certFile;
        	File pkFile;
        	if(!cfg.getProperty("ssl.cert", "").equals("") && !cfg.getProperty("ssl.pk", "").equals("")) {
        		certFile = new File(cfg.getProperty("ssl.cert"));
        		pkFile = new File(cfg.getProperty("ssl.pk"));
        	} else {
        		SelfSignedCertificate cert = new SelfSignedCertificate("robotbrain.info", new SecureRandom(), 2048);
        		certFile = cert.certificate();
        		pkFile = cert.privateKey();
        	}
            if(cfg.getProperty("ssl.cert").equals("") && cfg.getProperty("ssl.pk").equals("")){
                cfg.setProperty("ssl.cert",certFile.getPath());
                cfg.setProperty("ssl.pk",pkFile.getPath());
                try (FileWriter writer = new FileWriter("apoapsis.properties")) {
                    cfg.store(writer, "Apoapsis Settings");
                }
            }
            context = SslContext.newServerContext(certFile, pkFile);
        } else {
        	context = null;
        }
        int port = Integer.parseInt(cfg.getProperty("port", "25564"));

        try {
        	ServerBootstrap wsBootstrap = BootstrapHelper.createWebSocket(context, cfg.getProperty("token", "UNDEFINED"));
        	ServerBootstrap tcpBootstrap = BootstrapHelper.createTcp(context, cfg.getProperty("token", "UNDEFINED"));
        	//TODO: bind and sync
        } finally {
        	BootstrapHelper.close();
        }
    }
}