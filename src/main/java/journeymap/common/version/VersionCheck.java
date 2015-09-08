/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.common.version;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cpw.mods.fml.common.Loader;
import journeymap.common.Journeymap;
import journeymap.common.thread.JMThreadFactory;
// 1.8
//import net.minecraftforge.fml.common.Loader;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Checks to see if a newer released version of JourneyMap is available.
 */
public class VersionCheck
{
    private static volatile ExecutorService executorService;
    private static volatile Boolean updateCheckEnabled = Journeymap.proxy.isUpdateCheckEnabled();
    private static volatile Boolean versionIsCurrent = true;
    private static volatile Boolean versionIsChecked;
    private static volatile String versionAvailable;

    /**
     * Whether this build is the current version available.
     * @return
     */
    public static Boolean getVersionIsCurrent()
    {
        if (versionIsChecked == null)
        {
            checkVersion();
        }
        return versionIsCurrent;
    }

    /**
     * Whether the current version available has been checked.
     * @return
     */
    public static Boolean getVersionIsChecked()
    {
        if (versionIsChecked == null)
        {
            checkVersion();
        }
        return versionIsChecked;
    }

    /**
     * Gets the current version available.
     * @return
     */
    public static String getVersionAvailable()
    {
        if (versionIsChecked == null)
        {
            checkVersion();
        }
        return versionAvailable;
    }

    /**
     * Check for the current version by querying a JSON file that is
     * manually updated when a new version is released.
     */
    private static synchronized void checkVersion()
    {
        versionIsChecked = false;
        versionIsCurrent = true;
        versionAvailable = "0"; //$NON-NLS-1$

        if (!updateCheckEnabled)
        {
            Journeymap.getLogger().info("Update check disabled in properties file."); //$NON-NLS-1$
        }
        else
        {
            executorService = Executors.newSingleThreadExecutor(new JMThreadFactory("VersionCheck"));
            executorService.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    Journeymap.getLogger().info("Checking for updated version: " + Journeymap.VERSION_URL); //$NON-NLS-1$
                    InputStreamReader in = null;
                    HttpsURLConnection connection = null;
                    String rawResponse = null;
                    try
                    {
                        URL uri = URI.create(Journeymap.VERSION_URL).toURL();
                        connection = (HttpsURLConnection) uri.openConnection();
                        connection.setConnectTimeout(6000);
                        connection.setReadTimeout(6000);
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("User-Agent", createUserAgent());
                        in = new InputStreamReader(uri.openStream());
                        rawResponse = CharStreams.toString(in);

                        Gson gson = new GsonBuilder().create();
                        VersionData versionData = gson.fromJson(rawResponse, VersionData.class);

                        if (versionData.versions != null)
                        {
                            for (VersionLine versionLine : versionData.versions)
                            {
                                if (Loader.MC_VERSION.equals(versionLine.minecraft))
                                {
                                    versionAvailable = versionLine.journeymap;
                                    versionIsCurrent = isCurrent(Journeymap.JM_VERSION.toString(), versionAvailable);
                                    versionIsChecked = true;
                                    break;
                                }
                            }
                        }
                        else
                        {
                            Journeymap.getLogger().warn("Version URL had no data!"); //$NON-NLS-1$
                        }

                        Journeymap.getLogger().info(String.format("Current version online: JourneyMap %s for Minecraft %s on %s", versionAvailable, Loader.MC_VERSION, Journeymap.DOWNLOAD_URL));
                    }
                    catch (Throwable e)
                    {
                        Journeymap.getLogger().error("Could not check version URL", e); //$NON-NLS-1$
                        updateCheckEnabled = false;
                    }
                    finally
                    {
                        if (in != null)
                        {
                            try
                            {
                                in.close();
                                executorService.shutdown();
                                executorService = null;
                            }
                            catch (IOException e)
                            {
                            }
                        }
                    }

                    // Log newer version
                    if (!versionIsCurrent)
                    {
                        // TODO show once in chat when fullscreen map opened?
                    }
                }
            });
        }
    }

    /**
     * Whether this instance's version is current (equal or newer) to one avaialable.
     * @param thisVersionStr
     * @param availableVersionStr
     * @return
     */
    private static boolean isCurrent(String thisVersionStr, String availableVersionStr)
    {
        if (thisVersionStr.equals(availableVersionStr))
        {
            return true;
        }

        Version thisVersion = Version.from(thisVersionStr, null);
        Version availableVersion = Version.from(availableVersionStr, null);

        return !availableVersion.isNewerThan(thisVersion);
    }

    /**
     * Create the user agent string used by the HTTP connection.
     * @return
     */
    private static String createUserAgent()
    {
        String agent = null;

        try
        {
            // Get system properties
            String os = System.getProperty("os.name");
            if (os == null)
            {
                os = "";
            }

            String version = System.getProperty("os.version");
            if (version == null)
            {
                version = "";
            }

            String arch = System.getProperty("os.arch");
            if (arch == null)
            {
                arch = "";
            }
            if (arch.equals("amd64"))
            {
                arch = "WOW64";
            }

            String lang = String.format("%s_%s", System.getProperty("user.language", "en"), System.getProperty("user.country", "US"));

            // Build user agent string
            if (os.startsWith("Mac")) // Mac OS X, x86_64, ?
            {
                version = version.replace(".", "_");
                agent = String.format("Mozilla/5.0 (Macintosh; U; Intel Mac OS X %s; %s)", version, lang);
            }
            else if (os.startsWith("Win")) // Windows 7, amd64, 6.1
            {
                agent = String.format("Mozilla/5.0 (Windows; U; Windows NT %s; %s; %s)", version, arch, lang);
            }
            else if (os.startsWith("Linux")) // Linux, os.version = kernel version, os.arch = amd64
            {
                agent = String.format("Mozilla/5.0 (Linux; U; Linux %s; %s; %s)", version, arch, lang);
            }
            else
            {
                agent = String.format("Mozilla/5.0 (%s; U; %s %s; %s, %s)", os, os, version, arch, lang);
            }
        }
        catch (Throwable t)
        {
            agent = "Mozilla/5.0 (Unknown)";
        }

        return agent;
    }

    /**
     * Object model representing the JSON document.
     */
    class VersionData
    {
        VersionLine[] versions;
    }

    /**
     * Object model representing a version line in the JSON document.
     */
    class VersionLine
    {
        String minecraft;
        String journeymap;
    }
}
