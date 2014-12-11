package info.robotbrain.apoapsis;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

public class MCVersion implements Serializable
{
    public final URL downloadUrl;
    public final String name;

    public MCVersion(String baseUrl, String name) throws MalformedURLException
    {
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        this.downloadUrl = new URL(baseUrl + "/" + name + "/minecraft_server." + name + ".jar");
        this.name = name;
    }

}
