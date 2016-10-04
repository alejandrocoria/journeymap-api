package journeymap.server.properties;

import net.minecraftforge.common.DimensionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mysticdrew on 5/6/2016.
 */
public class PropertiesManager
{
    private static PropertiesManager INSTANCE;
    private Map<Integer, DimensionProperties> dimensionProperties;
    private GlobalProperties globalProperties;

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
        dimensionProperties = new HashMap<Integer, DimensionProperties>();
        globalProperties = new GlobalProperties();
        globalProperties.load().save();

        for (Integer dim : DimensionManager.getIDs())
        {
            DimensionProperties prop = new DimensionProperties(dim);
            dimensionProperties.put(dim, prop);
            prop.load();
        }
    }

    public DimensionProperties getDimProperties(int dim)
    {
        return dimensionProperties.get(dim);
    }

    public GlobalProperties getGlobalProperties()
    {
        return globalProperties;
    }
}
