package info.robotbrain.apoapsis;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Serializable
{
    private static final long serialVersionUID = 1L;
    public final File location;
    public final String name;
    public transient String uuid;
    public MCVersion version;
    transient ExecutorService service = Executors.newSingleThreadExecutor();
    transient ServerRun run;
    //private List<Mod> mods = new ArrayList<>();

    public Server(MCVersion version, File location, String name)
    {
        this.version = version;
        this.location = location;
        this.name = name;
    }

    /*public List<Mod> getMods()
    {
        return mods;
    }*/

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ServerRun run() throws Exception
    {
        if (this.run == null) {
            run = new ServerRun(this);
        }
        if (run.getStatus() != Status.NotRunning) {
            return run;
        }
        installVersion();
        new File(location, "mods").delete();
        new File(location, "coremods").delete();
        new File(location, "libraries").delete();
        /*for (Mod mod : mods) {
            if (!mod.compatableMCVersions.contains(version)) {
                throw new ModCompatabilityException(mod, version);
            }
            mod.install(run);
        }*/
        return run;
    }

    private void installVersion() throws IOException
    {
        File jar = new File(location, "minecraft-server.jar");
        URL url = version.downloadUrl;
        FileUtils.copyURLToFile(url, jar);
    }

    public enum Status
    {
        NotRunning,
        Init,
        Running,
        DeInit
    }
}
