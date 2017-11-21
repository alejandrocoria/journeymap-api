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

package journeymap.server.api.util;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.util.ClientPluginHelper;
import journeymap.common.api.util.PluginHelper;
import journeymap.server.api.IServerPlugin;
import journeymap.server.api.ServerPlugin;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Helper class used to detect and initialize IServerPlugins. A Server plugin class for JourneyMap must:
 * 1. Have the {@link journeymap.server.api.ServerPlugin} @annotation to be detected during initialization.
 * 2. Implement the {@link journeymap.server.api.IServerPlugin} interface.
 */
@ParametersAreNonnullByDefault
public class ServerPluginHelper extends PluginHelper<ServerPlugin, IServerPlugin>
{
    private static Supplier<ServerPluginHelper> lazyInit = Suppliers.memoize(ServerPluginHelper::new);

    private ServerPluginHelper()
    {
        super(ServerPlugin.class, IServerPlugin.class);
    }

    public static ServerPluginHelper instance()
    {
        return lazyInit.get();
    }
}
