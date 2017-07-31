/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.server;


import com.google.common.base.Joiner;
import journeymap.common.Journeymap;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;


/**
 * Created by Mysticdrew on 10/3/2016.
 */
public class Constants
{
    /**
     * The constant SERVER.
     */
    public static MinecraftServer SERVER = FMLCommonHandler.instance().getMinecraftServerInstance();
    private static final Joiner path = Joiner.on(File.separator).useForNull("");
    private static final String END = null;
    /**
     * The constant MC_DATA_DIR.
     */
    public static final File MC_DATA_DIR = SERVER.getDataDirectory();
    /**
     * The constant JOURNEYMAP_DIR.
     */
    public static String JOURNEYMAP_DIR = "journeymap";
    /**
     * The constant CONFIG_DIR.
     */
    public static String CONFIG_DIR = path.join(MC_DATA_DIR, JOURNEYMAP_DIR, "server", Journeymap.JM_VERSION.toMajorMinorString(), END);
}