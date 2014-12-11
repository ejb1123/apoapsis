package info.robotbrain.apoapsis;

public class ModCompatabilityException extends Exception
{

    public final Mod mod;
    public final MCVersion version;

    public ModCompatabilityException(Mod mod, MCVersion version)
    {
        super("Mod " + mod.name + " is not compatable with minecraft version " + version.name);
        this.mod = mod;
        this.version = version;
    }

}
