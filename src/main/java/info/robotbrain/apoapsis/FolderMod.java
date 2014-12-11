package info.robotbrain.apoapsis;

import org.apache.commons.io.FileUtils;

import java.io.File;

public class FolderMod extends Mod
{

    private String folder;
    private File source;

    public FolderMod(String name, String version, MCVersion[] mcVersions, File source, String folder)
    {
        super(name, version, mcVersions);
        this.source = source;
        this.folder = folder;
    }

    @Override
    public void install(ServerRun run) throws Exception
    {
        FileUtils.copyFileToDirectory(source, new File(run.getServer().location, folder));
    }

}
