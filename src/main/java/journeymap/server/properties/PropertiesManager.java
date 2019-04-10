/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.server.properties;

import net.minecraftforge.common.DimensionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mysticdrew on 5/6/2018.
 */
public class PropertiesManager
{
    private static PropertiesManager INSTANCE;
    private Map<Integer, DimensionProperties> dimensionProperties;
    private GlobalProperties globalProperties;

    private DefaultDimensionProperties defaultDimensionProperties;

    public static PropertiesManager getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new PropertiesManager();
            INSTANCE.loadConfigs();
        }
        return INSTANCE;
    }

    private void loadConfigs()
    {
        dimensionProperties = new HashMap<>();
        globalProperties = new GlobalProperties();
        globalProperties.load();
        defaultDimensionProperties = new DefaultDimensionProperties();
        defaultDimensionProperties.load();

        for (Integer dim : DimensionManager.getIDs())
        {
            genConfig(dim);
        }
    }

    public DimensionProperties getDimProperties(int dim)
    {
        if (dimensionProperties.get(dim) == null)
        {
            genConfig(dim);
        }
        return dimensionProperties.get(dim);
    }

    public DefaultDimensionProperties getDefaultDimensionProperties()
    {
        return defaultDimensionProperties;
    }

    public GlobalProperties getGlobalProperties()
    {
        return globalProperties;
    }

    private void genConfig(int dim)
    {
        DimensionProperties prop = new DimensionProperties(dim);
        dimensionProperties.put(dim, prop);
        if (!prop.getFile().exists())
        {
            prop.build();
        }
        prop.load();
    }
}
