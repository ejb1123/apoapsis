package info.robotbrain.apoapsis;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import info.robotbrain.apoapsis.Server.Status;
import info.robotbrain.apoapsis.ServerRun.Listener;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.util.UUID;

public class ApoapsisServerHandler extends ChannelDuplexHandler
{
    public Listener listener;
    Server currentServer;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception
    {
        System.out.println("Adding listener...");
        listener = new Listener()
        {
            @Override
            public void output(ServerRun run, String output)
            {
                ctx.writeAndFlush("out:" + run.getServer().uuid + ":" + output);
            }

            @Override
            public void error(ServerRun run, String output)
            {
                ctx.writeAndFlush("err:" + run.getServer().uuid + ":" + output);
            }

            @Override
            public void chat(ServerRun run, String player, String message)
            {
                ctx.writeAndFlush("chat:" + run.getServer().uuid + ":" + player + ":" + message);
            }

            @Override
            public void joined(ServerRun run, String player)
            {
                ctx.writeAndFlush("join:" + run.getServer().uuid + ":" + player);
            }

            @Override
            public void left(ServerRun run, String player)
            {
                ctx.writeAndFlush("part:" + run.getServer().uuid + ":" + player);
            }

            @Override
            public void stopped(ServerRun run)
            {
                ctx.writeAndFlush("stop:" + run.getServer().uuid);
            }

            @Override
            public void started(ServerRun run, double time)
            {
                ctx.writeAndFlush("started:" + run.getServer().uuid + ":" + time);
            }

            @Override
            public void exception(ServerRun run, Throwable cause)
            {
                ctx.writeAndFlush("ex:" + run.getServer().uuid + ":" + cause.getMessage());
                System.err.println("In server " + run.getServer().uuid);
                cause.printStackTrace();
            }

            @Override
            public void state(ServerRun run, Status status)
            {
                ctx.writeAndFlush("status:" + run.getServer().uuid + ":" + status.toString().toLowerCase());
            }

            @Override
            public void serverMsg(ServerRun run, String message)
            {
                ctx.writeAndFlush("message:" + run.getServer().uuid + ":" + message);
            }
        };
        ServerOrm.listeners.addListener(listener);
        super.handlerAdded(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        if (listener != null) {
            ServerOrm.listeners.removeListener(listener);
            listener = null;
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        String[] parts = ((String) msg).split(":", 2);
        String command = parts[0];
        switch (command) {
            case "select": {
                String uuid = parts[1];
                try {
                    currentServer = ServerOrm.get(uuid);
                } catch (NoSuchServerException unused) {
                    ctx.writeAndFlush("rx:err:nosuchserver");
                }
                ctx.writeAndFlush("rx:ok");
                break;
            }
            case "status": {
                if (currentServer == null) {
                    ctx.writeAndFlush("rx:err:noserverselected");
                    return;
                }
                ctx.writeAndFlush("rx:status:" + currentServer.run().getStatus().toString().toLowerCase());
                break;
            }
            case "list": {
                String it = parts[1];
                switch (it) {
                    case "players":
                        if (currentServer == null) {
                            ctx.writeAndFlush("rx:err:noserverselected");
                            return;
                        }
                        if (currentServer.run() == null || currentServer.run().getStatus() != Status.Running) {
                            ctx.writeAndFlush("rx:err:notrunning");
                            return;
                        }
                        ctx.writeAndFlush("rx:list:" + new Gson().toJson(currentServer.run().getPlayers()));
                        break;
                    case "servers":
                        ctx.writeAndFlush("rx:list:" + ServerOrm.listAsJson());
                        break;
                }
                break;
            }
            case "stop": {
                if (currentServer == null) {
                    ctx.writeAndFlush("rx:err:noserverselected");
                    return;
                }
                if (currentServer.run().getStatus() != Status.Running) {
                    ctx.writeAndFlush("rx:err:notrunning");
                    return;
                }
                currentServer.run().stop();
                ctx.writeAndFlush("rx:ok");
                break;
            }
            case "start": {
                if (currentServer == null) {
                    ctx.writeAndFlush("rx:err:noserverselected");
                    return;
                }
                if (currentServer.run().getStatus() != Status.NotRunning) {
                    ctx.writeAndFlush("rx:err:running");
                    return;
                }
                currentServer.run().start();
                ctx.writeAndFlush("rx:ok");
                break;
            }
            case "say": {
                if (currentServer == null) {
                    ctx.writeAndFlush("rx:err:noserverselected");
                    return;
                }
                if (currentServer.run().getStatus() != Status.Running) {
                    ctx.writeAndFlush("rx:err:notrunning");
                    return;
                }
                currentServer.run().say(parts[1]);
                ctx.writeAndFlush("rx:ok");
                break;
            }
            case "create": {
                if (currentServer != null) {
                    ctx.writeAndFlush("rx:warn:serverselected");
                }
                JsonParser parser = new JsonParser();
                JsonObject obj = parser.parse(parts[1]).getAsJsonObject();
                String loc = obj.get("location").getAsString();
                String name = obj.get("name").getAsString();
                JsonObject version = obj.getAsJsonObject("version");
                String vName = version.get("name").getAsString();
                String vBase = version.get("base").getAsString();
                MCVersion ver = new MCVersion(vBase, vName);
                File location = new File(loc);
                location.mkdirs();
                Server serv = new Server(ver, location, name);
                String uuid = ServerOrm.add(serv);
                currentServer = serv;
                ctx.writeAndFlush("rx:created:" + uuid);
                break;
            }            /*case "addMod": {                if (currentServer == null) {                    ctx.writeAndFlush("rx:err:noserverselected");                    return;                }                if (currentServer.run().getStatus() != Status.NotRunning) {                    ctx.writeAndFlush("rx:err:running");                    return;                }                try (ObjectInputStream in = new ObjectInputStream(                        new ByteArrayInputStream(Base64.getDecoder().decode(                                parts[1])))) {                    Mod mod = (Mod) in.readObject();                    currentServer.getMods().add(mod);                }                ctx.writeAndFlush("rx:ok");                break;            }            case "remMod": {                if (currentServer == null) {                    ctx.writeAndFlush("rx:err:noserverselected");                    return;                }                if (currentServer.run().getStatus() != Status.NotRunning) {                    ctx.writeAndFlush("rx:err:running");                    return;                }                String name = parts[1];                currentServer.getMods().removeIf(m -> Objects.equals(m.name, name));                break;            }*/
            case "cmd": {
                if (currentServer == null) {
                    ctx.writeAndFlush("rx:err:noserverselected");
                    return;
                }
                if (currentServer.run().getStatus() != Status.Running) {
                    ctx.writeAndFlush("rx:err:notrunning");
                    return;
                }
                currentServer.run().getInput().println(parts[1]);
                break;
            }
            case "changeversion": {
                if (currentServer == null) {
                    ctx.writeAndFlush("rx:err:noserverselected");
                    return;
                }
                if (currentServer.run().getStatus() != Status.NotRunning) {
                    ctx.writeAndFlush("rx:err:running");
                    return;
                }
                JsonParser parser = new JsonParser();
                JsonObject version = parser.parse(parts[1]).getAsJsonObject();
                String vName = version.get("name").getAsString();
                String vBase = version.get("base").getAsString();
                MCVersion ver = new MCVersion(vBase, vName);
                currentServer.version = ver;
                ctx.writeAndFlush("rx:changeversion:" + ver.name);
                break;
            }
            case "delete": {
                if (currentServer == null) {
                    ctx.writeAndFlush("rx:err:noserverselected");
                    return;
                }
                if (currentServer.run().getStatus() != Status.NotRunning) {
                    ctx.writeAndFlush("rx:err:running");
                    return;
                }
                File file = new File(currentServer.location, "server.apo");
                file.delete();
                UUID uuid = UUID.fromString(currentServer.uuid);
                ServerOrm.uuids.remove(currentServer.uuid);
                ServerOrm.servers.remove(uuid);
                ServerOrm.save();
                currentServer = null;
                ctx.writeAndFlush("rx:delete:ok");
                break;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        ctx.writeAndFlush("rx:ex:" + cause.getMessage());
        cause.printStackTrace();
    }
}