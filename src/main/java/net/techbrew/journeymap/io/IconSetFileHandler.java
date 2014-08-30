package net.techbrew.journeymap.io;

import com.google.common.base.Joiner;
import cpw.mods.fml.client.FMLClientHandler;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Mark on 8/29/2014.
 */
public class IconSetFileHandler
{
    public static final String ASSETS_JOURNEYMAP_ICON_ENTITY = "/assets/journeymap/icon/entity";
    public final static String MOB_ICON_SET_2D = "2D";
    public final static String MOB_ICON_SET_3D = "3D";

    public static final String ASSETS_JOURNEYMAP_ICON_SKIN = "/assets/journeymap/icon/skin";
    public final static String SKIN_VICTORIAN = "Victorian";

    public static void initialize()
    {
        JourneyMap.getLogger().info("Initializing icon sets...");

        // Skin icons
        for (String setName : Arrays.asList(SKIN_VICTORIAN))
        {
            copyResources(getSkinIconDir(), ASSETS_JOURNEYMAP_ICON_SKIN, setName);
        }

        // Mob icons
        for (String setName : Arrays.asList(MOB_ICON_SET_2D, MOB_ICON_SET_3D))
        {
            copyResources(getEntityIconDir(), ASSETS_JOURNEYMAP_ICON_ENTITY, setName);
        }
    }

    private static void copyResources(File targetDirectory, String assetsPath, String setName)
    {
        try
        {
            URL resourceDir = JourneyMap.class.getResource(assetsPath);
            String toPath = String.format("%s/%s", assetsPath, setName);
            File toDir = new File(targetDirectory, setName);
            boolean inJar = FileHandler.isInJar();
            if (inJar)
            {
                String fromPath = resourceDir.getPath().split("file:")[1].split("!/")[0];
                FileHandler.copyFromZip(fromPath, toPath, toDir, false);
            }
            else
            {
                File fromDir = new File(JourneyMap.class.getResource(toPath).getFile());
                FileHandler.copyFromDirectory(fromDir, toDir, false);
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().warn("Couldn't unzip icon set for " + setName + ": " + t);
        }
    }

    public static File getEntityIconDir()
    {
        File dir = new File(FMLClientHandler.instance().getClient().mcDataDir, Constants.ENTITY_ICON_DIR);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getSkinIconDir()
    {
        File dir = new File(FMLClientHandler.instance().getClient().mcDataDir, Constants.SKIN_ICON_DIR);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        return dir;
    }

    public static ArrayList<String> getEntityIconSetNames()
    {
        return getIconSetNames(getEntityIconDir(), MOB_ICON_SET_2D, MOB_ICON_SET_3D);
    }

    public static ArrayList<String> getSkinIconSetNames()
    {
        return getIconSetNames(getSkinIconDir(), SKIN_VICTORIAN);
    }

    public static ArrayList<String> getIconSetNames(File parentDir, String... defaultIconSets)
    {
        try
        {
            // Initialize entity iconset folders
            for (String iconSetName : defaultIconSets)
            {
                File iconSetDir = new File(parentDir, iconSetName);
                if (iconSetDir.exists() && !iconSetDir.isDirectory())
                {
                    iconSetDir.delete();
                }
                iconSetDir.mkdirs();
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().error("Could not prepare iconset directories for " + parentDir + ": " + LogFormatter.toString(t));
        }

        // Create list of icon set names
        ArrayList<String> names = new ArrayList<String>();
        for (File iconSetDir : parentDir.listFiles())
        {
            if (iconSetDir.isDirectory())
            {
                names.add(iconSetDir.getName());
            }
        }
        Collections.sort(names);

        return names;
    }

    public static BufferedImage getIconFromFile(File parentdir, String assetsPath, String setName, String iconPath, BufferedImage defaultImg)
    {
        String filePath = Joiner.on(File.separatorChar).join(setName, iconPath.replace('/', File.separatorChar));
        File iconFile = new File(parentdir, filePath);

        BufferedImage img = null;
        if (iconFile.exists())
        {
            img = FileHandler.getImage(iconFile);
        }

        if (img == null)
        {
            img = getIconFromResource(assetsPath, setName, iconPath);
            if (img == null)
            {
                img = defaultImg;
            }

            try
            {
                iconFile.getParentFile().mkdirs();
                ImageIO.write(img, "png", iconFile);
            }
            catch (Exception e)
            {
                String error = Constants.getMessageJMERR00("Can't write entity icon" + iconFile + ": " + e);
                JourneyMap.getLogger().error(error);
            }

            JourneyMap.getLogger().debug("Created entity icon: " + iconFile);
        }

        return img;
    }

    public static BufferedImage getIconFromResource(String assetsPath, String setName, String iconPath)
    {
        try
        {
            InputStream is = getIconStream(assetsPath, setName, iconPath);
            if (is == null)
            {
                return null;
            }
            BufferedImage img = ImageIO.read(is);
            is.close();
            return img;
        }
        catch (IOException e)
        {
            String error = Constants.getMessageJMERR17(e.getMessage());
            JourneyMap.getLogger().error(error);
            return null;
        }
    }

    public static InputStream getIconStream(String assetsPath, String setName, String iconPath)
    {
        try
        {
            String pngPath = Joiner.on('/').join(assetsPath, setName, iconPath);
            InputStream is = JourneyMap.class.getResourceAsStream(pngPath);
            if (is == null)
            {
                JourneyMap.getLogger().warn(String.format("Icon Set asset not found: " + pngPath));
                return null;
            }
            return is;
        }
        catch (Throwable e)
        {
            String error = Constants.getMessageJMERR17(e.getMessage());
            JourneyMap.getLogger().error(error);
            return null;
        }
    }
}