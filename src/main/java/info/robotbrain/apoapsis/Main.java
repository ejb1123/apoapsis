package info.robotbrain.apoapsis;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
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
            cfg.setProperty("ws.port", "25564");
            cfg.setProperty("tcp.port", "25563");
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
            if (!cfg.getProperty("ssl.cert", "").equals("") && !cfg.getProperty("ssl.pk", "").equals("")) {
                certFile = new File(cfg.getProperty("ssl.cert"));
                pkFile = new File(cfg.getProperty("ssl.pk"));
            } else {
                SelfSignedCertificate cert = new SelfSignedCertificate("robotbrain.info", new SecureRandom(), 2048);
                certFile = cert.certificate();
                pkFile = cert.privateKey();
            }
            if (cfg.getProperty("ssl.cert").equals("") && cfg.getProperty("ssl.pk").equals("")) {
                cfg.setProperty("ssl.cert", certFile.getPath());
                cfg.setProperty("ssl.pk", pkFile.getPath());
                try (FileWriter writer = new FileWriter("apoapsis.properties")) {
                    cfg.store(writer, "Apoapsis Settings");
                }
            }
            context = SslContext.newServerContext(certFile, pkFile);
        } else {
            context = null;
        }
        int wsPort = Integer.parseInt(cfg.getProperty("ws.port", "25564"));
        int tcpPort = Integer.parseInt(cfg.getProperty("tcp.port", "25563"));
        try {
            ServerBootstrap wsBootstrap = BootstrapHelper.createWebSocket(context, cfg.getProperty("token", "UNDEFINED"));
            ServerBootstrap tcpBootstrap = BootstrapHelper.createTcp(context, cfg.getProperty("token", "UNDEFINED"));

            ChannelFuture wsClose = wsBootstrap.bind(wsPort).sync().channel().closeFuture();
            ChannelFuture tcpClose = tcpBootstrap.bind(tcpPort).sync().channel().closeFuture();

            wsClose.sync();
            tcpClose.sync();

        } finally {
            BootstrapHelper.close();
        }
    }
}