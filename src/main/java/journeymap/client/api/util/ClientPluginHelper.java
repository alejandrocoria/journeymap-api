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

package journeymap.client.api.util;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientPlugin;
import journeymap.common.api.util.PluginHelper;

/**
 * Helper class used to detect and initialize IClientPlugins. A Client plugin class for JourneyMap must:
 * 1. Have the {@link journeymap.client.api.ClientPlugin} @annotation to be detected during initialization.
 * 2. Implement the {@link journeymap.client.api.IClientPlugin} interface.
 */
public class ClientPluginHelper extends PluginHelper<ClientPlugin, IClientPlugin>
{
    private static Supplier<ClientPluginHelper> lazyInit = Suppliers.memoize(ClientPluginHelper::new);

    private ClientPluginHelper()
    {
        super(ClientPlugin.class, IClientPlugin.class);
    }

    public static ClientPluginHelper instance()
    {
        return lazyInit.get();
    }
}
