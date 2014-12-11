package info.robotbrain.apoapsis;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class Mod implements Serializable
{
    public final String name;
    public final String version;
    public final List<MCVersion> compatableMCVersions;

    public Mod(String name, String version, MCVersion[] mcVersions)
    {
        this.name = name;
        this.version = version;
        compatableMCVersions = Collections.unmodifiableList(Arrays
                .asList(mcVersions));
    }

    public abstract void install(ServerRun run) throws Exception;
}
