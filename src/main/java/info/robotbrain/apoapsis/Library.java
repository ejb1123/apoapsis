/*package info.robotbrain.apoapsis;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

public final class Library implements Serializable
{
    public final String group;
    public final String name;
    public final String version;
    public final String classifier;
    public final String repo;

    public Library(String group, String name, String version, String classifier, String repo)
    {
        this.group = group;
        this.name = name;
        this.version = version;
        this.classifier = classifier;
        this.repo = repo;
    }

    public URL loc() throws MalformedURLException
    {
        String it = repo + "/" + group.replace('.', '/') + "/" + name + "/" + version + "/" + name + "-" + version;
        if (classifier != null && !"".equals(classifier)) {
            it += "-" + classifier;
        }
        it += ".jar";
        return new URL(it);
    }
}*/