/*
 * JourneyMap API (http://journeymap.info)
 * http://bitbucket.org/TeamJM/journeymap-api
 *
 * Copyright (c) 2011-2016 Techbrew.  All Rights Reserved.
 * The following limited rights are granted to you:
 *
 * You MAY:
 *  + Write your own code that uses the API source code in journeymap.* packages as a dependency.
 *  + Write and distribute your own code that uses, modifies, or extends the example source code in example.* packages
 *  + Fork and modify any source code for the purpose of submitting Pull Requests to the TeamJM/journeymap-api repository.
 *    Submitting new or modified code to the repository means that you are granting Techbrew all rights to the submitted code.
 *
 * You MAY NOT:
 *  - Distribute source code or classes (whether modified or not) from journeymap.* packages.
 *  - Submit any code to the TeamJM/journeymap-api repository with a different license than this one.
 *  - Use code or artifacts from the repository in any way not explicitly granted by this license.
 *
 */

package journeymap.common.api.util;

import com.google.common.base.Strings;
import journeymap.common.api.IJmAPI;
import journeymap.common.api.IJmPlugin;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

/**
 * Helper class used by JourneyMap to load and initialize plugins.
 * @param <A> Annotation used for plugin discovery
 * @param <I> Plugin interface
 */
@ParametersAreNonnullByDefault
public abstract class PluginHelper<A,I extends IJmPlugin>
{
    public final static Logger LOGGER = LogManager.getLogger(IJmAPI.API_OWNER);
    private final Class<A> pluginAnnotationClass;
    private final Class<I> pluginInterfaceClass;

    protected Map<String, I> plugins = null;
    protected boolean initialized;

    protected PluginHelper(Class<A> pluginAnnotationClass, Class<I> pluginInterfaceClass)
    {
        this.pluginAnnotationClass = pluginAnnotationClass;
        this.pluginInterfaceClass = pluginInterfaceClass;
    }
    
    /**
     * Called by JourneyMap during it's preInitialization phase to find plugin classes
     * included in other mods and then instantiate them.
     * <p>
     * Mods which are testing integration can also call this in a dev environment
     * and pass in a stub implementation, but must never do so in production code.
     *
     * @param asmDataTable asmDataTable
     * @return map of instantiated plugins, keyed by modId
     */
    public Map<String, I> preInitPlugins(ASMDataTable asmDataTable)
    {
        if (plugins == null)
        {
            HashMap<String, I> discovered = new HashMap<>();
            Set<ASMDataTable.ASMData> asmDataSet = asmDataTable.getAll(pluginAnnotationClass.getCanonicalName());

            String pluginAnnotationName = pluginAnnotationClass.getCanonicalName();
            String pluginInterfaceName = pluginInterfaceClass.getSimpleName();
            for (ASMDataTable.ASMData asmData : asmDataSet)
            {
                String className = asmData.getClassName();
                try
                {
                    Class<?> pluginClass = Class.forName(className);
                    if (pluginInterfaceClass.isAssignableFrom(pluginClass))
                    {
                        Class<I> interfaceImplClass = (Class<I>) pluginClass.asSubclass(pluginInterfaceClass);
                        I instance = (I) interfaceImplClass.newInstance();
                        String modId = instance.getModId();
                        if (Strings.isNullOrEmpty(modId))
                        {
                            throw new Exception("IPlugin.getModId() must return a non-empty, non-null value");
                        }
                        if (discovered.containsKey(modId))
                        {
                            Class otherPluginClass = discovered.get(modId).getClass();
                            throw new Exception(String.format("Multiple plugins trying to use the same modId: %s and %s", interfaceImplClass, otherPluginClass));
                        }
                        discovered.put(modId, instance);
                        LOGGER.info(String.format("Found @%s: %s", pluginAnnotationName, className));
                    }
                    else
                    {
                        LOGGER.error(String.format("Found @%s: %s, but it doesn't implement %s",
                                pluginAnnotationName, className, pluginInterfaceName));
                    }
                }
                catch (Exception e)
                {
                    LOGGER.error(String.format("Found @%s: %s, but failed to instantiate it: %s",
                            pluginAnnotationName, className, e.getMessage()), e);
                }
            }

            if(discovered.isEmpty())
            {
                LOGGER.info("No plugins for JourneyMap API discovered.");
            }

            plugins = Collections.unmodifiableMap(discovered);
        }

        return plugins;
    }

    /**
     * Called by JourneyMap during its initialization phase.  Can only be called once per runtime per side.
     * <p>
     * Mods which are testing integration can also call this in a dev environment
     * and pass in a stub implementation, but must never do so in production code.
     *
     * @param jmApi JourneyMap API implementation
     * @return list of initialized plugins, null if plugin discovery never occurred
     */
    public Map<String, I> initPlugins(IJmAPI jmApi)
    {
        if (plugins == null)
        {
            // Exception used just to show a trace back to whoever shouldn't have called this.
            LOGGER.warn("Plugin discovery never occurred.", new IllegalStateException());
        }
        else if (!initialized)
        {
            LOGGER.info(String.format("Initializing plugins with %s", jmApi.getClass().getName()));

            HashMap<String, I> discovered = new HashMap<>(plugins);
            Iterator<I> iter = discovered.values().iterator();
            while (iter.hasNext())
            {
                I plugin = iter.next();
                try
                {
                    plugin.initialize(jmApi);
                    LOGGER.info(String.format("Initialized %s: %s", pluginInterfaceClass.getSimpleName(), plugin.getClass().getName()));
                }
                catch (Exception e)
                {
                    LOGGER.error("Failed to initialize I: " + plugin.getClass().getName(), e);
                    iter.remove();
                }
            }

            // Finalize the list
            plugins = Collections.unmodifiableMap(discovered);
            initialized = true;
        }
        else
        {
            LOGGER.info("Plugins already initialized.");
        }

        return plugins;
    }

    /**
     * Get the map of plugins, keyed by modId.
     *
     * @return null if {@link #preInitPlugins(ASMDataTable)} hasn't been called yet
     */
    public Map<String, I> getPlugins()
    {
        return plugins;
    }
}
