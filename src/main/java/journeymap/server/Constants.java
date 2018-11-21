/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
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
    public static MinecraftServer SERVER = FMLCommonHandler.instance().getMinecraftServerInstance();
    private static final Joiner path = Joiner.on(File.separator).useForNull("");
    private static final String END = null;
    public static final File MC_DATA_DIR = SERVER.getDataDirectory();
    public static String JOURNEYMAP_DIR = "journeymap";
    public static String CONFIG_DIR = path.join(MC_DATA_DIR, JOURNEYMAP_DIR, "server", Journeymap.JM_VERSION.toMajorMinorString(), END);
}
