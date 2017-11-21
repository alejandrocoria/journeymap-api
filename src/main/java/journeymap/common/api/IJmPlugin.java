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

package journeymap.common.api;

import journeymap.client.api.IClientAPI;
import journeymap.server.api.IServerAPI;
import journeymap.server.api.ServerPlugin;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Base interface for a JourneyMap plugin implementation (both client and server).
 * @param <I> Sided API of JourneyMap
 * @see journeymap.client.api.IClientPlugin
 * @see journeymap.server.api.IServerPlugin
 */
@ParametersAreNonnullByDefault
public interface IJmPlugin<I extends IJmAPI>
{
    /**
     * Used by JourneyMap to associate mod id with a plugin instance.
     */
    String getModId();

    /**
     * Called by JourneyMap during the init phase of Forge mod loading.  Your implementation
     * should retain a reference to the API passed in, since that is what your plugin
     * will use to make calls to JourneyMap.
     *
     * @param jmApi API implementation
     */
    void initialize(final I jmApi);
}
