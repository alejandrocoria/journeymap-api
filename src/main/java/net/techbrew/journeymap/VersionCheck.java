/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Loader;
import net.techbrew.journeymap.log.ChatLog;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.thread.JMThreadFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.Level;

public class VersionCheck
{
    private static ExecutorService executorService;
    private static volatile Boolean updateCheckEnabled = JourneyMap.getCoreProperties().checkUpdates.get();
    private static volatile Boolean versionIsCurrent = true;
    private static volatile Boolean versionIsChecked;
    private static volatile String versionAvailable;

    public static Boolean getVersionIsCurrent()
    {
        if (versionIsChecked == null)
        {
            checkVersion();
        }
        return versionIsCurrent;
    }

    public static Boolean getVersionIsChecked()
    {
        if (versionIsChecked == null)
        {
            checkVersion();
        }
        return versionIsChecked;
    }

    public static String getVersionAvailable()
    {
        if (versionIsChecked == null)
        {
            checkVersion();
        }
        return versionAvailable;
    }

    private static synchronized void checkVersion()
    {
        versionIsChecked = false;
        versionIsCurrent = true;
        versionAvailable = "0"; //$NON-NLS-1$

        if (!updateCheckEnabled)
        {
            JourneyMap.getLogger().info("Update check disabled in properties file."); //$NON-NLS-1$
        }
        else
        {
            executorService = Executors.newSingleThreadExecutor(new JMThreadFactory("VersionCheck"));
            executorService.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    JourneyMap.getLogger().info("Checking for updated version: " + JourneyMap.VERSION_URL); //$NON-NLS-1$
                    InputStreamReader in = null;
                    HttpsURLConnection connection = null;
                    try
                    {
                        URL uri = URI.create(JourneyMap.VERSION_URL).toURL();
                        connection = (HttpsURLConnection) uri.openConnection();
                        connection.setConnectTimeout(6000);
                        connection.setReadTimeout(6000);
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("User-Agent", createUserAgent());
                        in = new InputStreamReader(uri.openStream());

                        Gson gson = new GsonBuilder().create();
                        VersionData versionData = gson.fromJson(in, VersionData.class);

                        if(versionData.versions!=null)
                        {
                            for (VersionLine versionLine : versionData.versions)
                            {
                                if(Loader.MC_VERSION.equals(versionLine.minecraft))
                                {
                                    versionAvailable = versionLine.journeymap;
                                    versionIsCurrent = isCurrent(JourneyMap.JM_VERSION, versionAvailable);
                                    versionIsChecked = true;
                                    break;
                                }
                            }
                        }
                        else
                        {
                            JourneyMap.getLogger().warn("Version URL had no data!"); //$NON-NLS-1$
                        }

                        JourneyMap.getLogger().info("For Minecraft " + Loader.MC_VERSION + ", JourneyMap version available online: " + versionAvailable); //$NON-NLS-1$
                    }
                    catch (Throwable e)
                    {
                        JourneyMap.getLogger().log(Level.ERROR, "Could not check version URL", e); //$NON-NLS-1$
                        updateCheckEnabled = false;
                    }
                    finally
                    {
                        if(in!=null)
                        {
                            try
                            {
                                in.close();
                            }
                            catch (IOException e)
                            {
                            }
                        }
                    }

                    // Announce newer version
                    if (!versionIsCurrent)
                    {
                        ChatLog.announceI18N(Constants.getString("jm.common.new_version_available", versionAvailable)); //$NON-NLS-1$
                        ChatLog.announceURL(JourneyMap.WEBSITE_URL, JourneyMap.WEBSITE_URL);
                    }
                }
            });
        }
    }

    class VersionData
    {
        VersionLine[] versions;
    }

    class VersionLine
    {
        String minecraft;
        String journeymap;
    }

    private static boolean isCurrent(String thisVersion, String availableVersion)
    {
        if(thisVersion.startsWith("@"))
        {
            thisVersion = availableVersion + "dev";
        }

        if(thisVersion.equals(availableVersion))
        {
            return true;
        }

        int[] thisVersionArr = toVersionArray(thisVersion);
        int[] availableVersionArr = toVersionArray(availableVersion);
        for(int i=0; i<availableVersionArr.length; i++)
        {
            if(availableVersionArr[i] > thisVersionArr[i])
            {
                return false;
            }
        }
        return true;
    }

    private static int[] toVersionArray(String versionString)
    {
        String[] strings = versionString.trim().split("\\D+"); // split on all non-numerics
        int[] ints = new int[strings.length];
        boolean errorsFound = false;
        for(int i=0; i<strings.length; i++)
        {
            try
            {
                if(Strings.isNullOrEmpty(strings[i]))
                {
                    ints[i] = 0;
                    errorsFound = true;
                }
                else
                {
                    ints[i] = Integer.parseInt(strings[i]);
                }
            }
            catch(NumberFormatException e)
            {
                ints[i] = 0;
                errorsFound = true;
            }
        }

        if(errorsFound)
        {
            JourneyMap.getLogger().warn(String.format("Version had problems when parsed. In: %s , Out: %s", versionString, ints)); //$NON-NLS-1$
        }
        return ints;
    }

    private static String createUserAgent()
    {
        String agent = null;

        try
        {
            // Get system properties
            String os = System.getProperty("os.name");
            if(os==null) os = "";

            String version = System.getProperty("os.version");
            if(version==null) version = "";

            String arch = System.getProperty("os.arch");
            if(arch==null) arch = "";
            if(arch.equals("amd64")) arch = "WOW64";

            String lang = String.format("%s_%s", System.getProperty("user.language"), System.getProperty("user.country"));
            if(lang.contains("null")) {
                lang = FMLClientHandler.instance().getCurrentLanguage();
            }

            // Build user agent string
            if(os.startsWith("Mac")) // Mac OS X, x86_64, ?
            {
                version = version.replace(".","_");
                agent = String.format("Mozilla/5.0 (Macintosh; U; Intel Mac OS X %s; %s)", version, lang);
            }
            else if(os.startsWith("Win")) // Windows 7, amd64, 6.1
            {
                agent = String.format("Mozilla/5.0 (Windows; U; Windows NT %s; %s; %s)", version, arch, lang);
            }
            else if(os.startsWith("Linux")) // Linux, os.version = kernel version, os.arch = amd64
            {
                agent = String.format("Mozilla/5.0 (Linux; U; Linux %s; %s; %s)", version, arch, lang);
            }
            else
            {
                agent = String.format("Mozilla/5.0 (%s; U; %s %s; %s, %s)", os, os, version, arch, lang);
            }
        }
        catch(Throwable t)
        {
            agent = "Mozilla/5.0 (Unknown)";
        }

        return agent;
    }

    public static void launchWebsite()
    {
        String url = JourneyMap.WEBSITE_URL;
        try
        {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        }
        catch (Throwable e)
        {
            JourneyMap.getLogger().log(Level.ERROR, "Could not launch browser with URL: " + url, LogFormatter.toString(e)); //$NON-NLS-1$
        }
    }
}
