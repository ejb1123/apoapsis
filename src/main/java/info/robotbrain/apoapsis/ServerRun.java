package info.robotbrain.apoapsis;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import info.robotbrain.apoapsis.Server.Status;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerRun
{
    private final Server server;
    private final Set<String> players = Sets.newHashSet();
    public ListenableFuture<Boolean> future;
    private List<String> args;
    private PrintWriter input;
    private Server.Status status = Status.NotRunning;
    private static OutputHandler[] handlers = { new OutputHandler()
    {
        final Pattern chat = Pattern.compile("\\[\\d+:\\d+:\\d+] \\[Server thread/INFO\\]: <(?<player>[a-zA-Z0-9_-]+)> (?<text>.*)");

        @Override
        public void processOutput(ServerRun run, String output)
        {
            Matcher matcher = chat.matcher(output);
            if (matcher.matches()) {
                String player = matcher.group("player");
                String text = matcher.group("text");
                ServerOrm.listeners.fire().chat(run, player, text);
            }
        }
    }, new OutputHandler()
    {
        final Pattern left = Pattern.compile("\\[\\d+:\\d+:\\d+] \\[Server thread/INFO]: (?<player>[a-zA-Z0-9-_]+) left the game");

        @Override
        public void processOutput(ServerRun run, String output)
        {
            Matcher matcher = left.matcher(output);
            if (matcher.matches()) {
                String player = matcher.group("player");
                run.players.remove(player);
                ServerOrm.listeners.fire().left(run, player);
            }
        }
    }, new OutputHandler()
    {
        final Pattern left = Pattern.compile("\\[\\d+:\\d+:\\d+] \\[Server thread/INFO]: (?<player>[a-zA-Z0-9-_]+) joined the game");

        @Override
        public void processOutput(ServerRun run, String output)
        {
            Matcher matcher = left.matcher(output);
            if (matcher.matches()) {
                String player = matcher.group("player");
                run.players.add(player);
                ServerOrm.listeners.fire().joined(run, player);
            }
        }
    }, new OutputHandler()
    {
        final Pattern done = Pattern.compile("\\[\\d+:\\d+:\\d+] \\[Server thread/INFO]: Done \\((?<time>\\d+(?:\\.\\d+)?)s\\)! For help, type \"help\" or \"\\?\"");

        @Override
        public void processOutput(ServerRun run, String output)
        {
            Matcher matcher = done.matcher(output);
            if (matcher.matches()) {
                String time = matcher.group("time");
                ServerOrm.listeners.fire().started(run, Double.parseDouble(time));
                run.status = Status.Running;
                ServerOrm.listeners.fire().state(run, run.status);
            }
        }
    }, new OutputHandler()
    {
        final Pattern stopping = Pattern.compile("\\[\\d+:\\d+:\\d+] \\[Server Shutdown Thread/INFO]: Stopping server");

        @Override
        public void processOutput(ServerRun run, String output)
        {
            Matcher matcher = stopping.matcher(output);
            if (matcher.matches()) {
                run.status = Status.DeInit;
                ServerOrm.listeners.fire().state(run, run.status);
            }
        }
    }/*, new OutputHandler()     {     @Override public void processOutput(ServerRun run, String output)     {     System.out.println(run.server.location + ": " + output);     }     @Override public void processError(ServerRun run, String output)     {     System.err.println(run.server.location + ": " + output);     }     }*/ };

    public ServerRun(Server server)
    {
        this.server = server;
        args = new ArrayList<>();
        addArgs("java", "-jar", "minecraft-server.jar", "--nogui");
    }

    public Set<String> getPlayers()
    {
        return Collections.unmodifiableSet(players);
    }

    public Server.Status getStatus()
    {
        return status;
    }

    public void addArgs(String... args)
    {
        Collections.addAll(this.args, args);
    }    /*public void installLibs(Library... libs) throws            IOException    {        File libsDir = new File(getServer().location, "libraries");        for (Library lib : libs) {            File it = new File(libsDir, lib.group.replace('.', '/'));            it = new File(it, lib.name);            it = new File(it, lib.version);            //noinspection ResultOfMethodCallIgnored            it.mkdirs();            it = new File(it, lib.name + "-" + lib.version + "-"                    + lib.classifier + ".jar");            if (!it.exists()) {                FileUtils.copyURLToFile(lib.loc(), it);            }        }    }*/

    public ServerRun start()
    {
        try {
            try (PrintWriter writer = new PrintWriter(new FileWriter(new File(server.location, "eula.txt")))) {
                writer.println("eula=true");
            }
            status = Status.Init;
            ServerOrm.listeners.fire().state(this, status);
            Future<Boolean> submit = getServer().service.submit(this::run);
            future = JdkFutureAdapters.listenInPoolThread(submit);
            Futures.addCallback(future, new FutureCallback<Boolean>()
            {
                @Override
                public void onSuccess(Boolean result)
                {
                    ServerOrm.listeners.fire().stopped(ServerRun.this);
                    status = Status.NotRunning;
                    ServerOrm.listeners.fire().state(ServerRun.this, status);
                }

                @Override
                public void onFailure(Throwable t)
                {
                    ServerOrm.listeners.fire().exception(ServerRun.this, t);
                    status = Status.NotRunning;
                    ServerOrm.listeners.fire().state(ServerRun.this, status);
                }
            });
        } catch (Throwable t) {
            status = Status.NotRunning;
            throw Throwables.propagate(t);
        }
        return this;
    }

    private boolean run() throws IOException, InterruptedException
    {
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.directory(getServer().location);
        Process p = builder.start();
        this.input = new PrintWriter(p.getOutputStream(), true);
        connect(l -> {
            for (OutputHandler handler : handlers) {
                handler.processOutput(this, l);
            }
            output(l, false);
        }, p.getInputStream());
        connect(l -> {
            for (OutputHandler handler : handlers) {
                handler.processError(this, l);
            }
            output(l, true);
        }, p.getErrorStream());
        return p.waitFor() == 0;
    }

    private void output(String l, boolean b)
    {
        if (b) {
            ServerOrm.listeners.fire().error(this, l);
        } else {
            ServerOrm.listeners.fire().output(this, l);
        }
    }

    private void connect(Consumer<String> consumer, InputStream inputStream)
    {
        Runnable runnable = () -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            while (true) {
                String line;
                try {
                    line = reader.readLine();
                } catch (IOException e) {
                    throw new IOError(e);
                }
                if (line == null) {
                    return;
                }
                consumer.accept(line);
            }
        };
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        t.start();
    }

    public PrintWriter getInput()
    {
        return input;
    }

    public void stop()
    {
        input.println("stop");
    }

    public void say(String it)
    {
        input.printf("/say %s%n", it);
    }

    public Server getServer()
    {
        return server;
    }

    public interface Listener
    {
        public void output(ServerRun run, String output);

        public void error(ServerRun run, String output);

        public void chat(ServerRun run, String player, String message);

        public void joined(ServerRun run, String player);

        public void left(ServerRun run, String player);

        public void stopped(ServerRun run);

        public void started(ServerRun run, double time);

        public void exception(ServerRun run, Throwable throwable);

        public void serverMsg(ServerRun run, String message);

        public void state(ServerRun run, Status status);
    }

    @SuppressWarnings("UnusedParameters")
    static abstract class OutputHandler
    {
        public void processOutput(ServerRun run, String output)
        {
        }

        public void processError(ServerRun run, String output)
        {
        }
    }
}