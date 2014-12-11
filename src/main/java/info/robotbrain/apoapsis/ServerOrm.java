package info.robotbrain.apoapsis;

import info.robotbrain.apoapsis.Server.Status;
import info.robotbrain.apoapsis.ServerRun.Listener;
import org.apache.commons.lang3.event.EventListenerSupport;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;

public class ServerOrm
{
    static Map<UUID, Server> servers = new HashMap<>();
    static Properties uuids;

    static EventListenerSupport<Listener> listeners = new EventListenerSupport<>(
            Listener.class);

    public static void init() throws IOException
    {
        if (uuids == null) {
            uuids = new Properties();
            if (new File("servers.properties").exists()) {
                uuids.load(new FileReader("servers.properties"));
            }
        }
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                for (Server server : servers.values()) {
                    if (server.run != null && server.run.getStatus() != Status.NotRunning) {
                        server.run.stop();
                    }
                }
            }
        });
    }

    public static Server get(String uuid) throws IOException,
            ClassNotFoundException, NoSuchServerException
    {
        UUID it = UUID.fromString(uuid);
        if (servers.containsKey(it)) {
            return servers.get(it);
        }
        if (!uuids.stringPropertyNames().contains(uuid)) {
            throw new NoSuchServerException();
        }
        File file = new File(uuids.getProperty(uuid), "server.apo");
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(
                file))) {
            Server server = (Server) in.readObject();
            server.uuid = uuid;
            server.service = Executors.newCachedThreadPool();
            servers.put(it, server);
            return server;
        }
    }

    public static String add(Server serv) throws IOException
    {
        UUID uuid = UUID.randomUUID();
        uuids.setProperty(uuid.toString(), serv.location.getAbsolutePath());
        uuids.store(new FileWriter("servers.properties"),
                "Apoapsis Server List");
        servers.put(uuid, serv);
        serv.uuid = uuid.toString();
        writeServer(serv);
        return uuid.toString();
    }

    private static void writeServer(Server serv) throws IOException
    {
        File file = new File(serv.location, "server.apo");
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(file))) {
            out.writeObject(serv);
        }
    }
}
