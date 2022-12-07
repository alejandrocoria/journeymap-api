/*
 * JourneyMap API (http://journeymap.info)
 * http://github.com/TeamJM/journeymap-api
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

package journeymap.client.api;

import journeymap.client.api.event.ClientEvent;

import java.util.EnumSet;

/**
 * Interface used by JourneyMap to initialize client plugins and provide the Client API.
 */
public interface IClientPlugin
{
    /**
     * Called by JourneyMap during the init phase of mod loading.  Your implementation
     * should retain a reference to the IClientAPI passed in, since that is what your plugin
     * will use to add overlays, etc. to JourneyMap.
     * <p>
     * This is also a good time to call {@link IClientAPI#subscribe(String, EnumSet)} to subscribe to any
     * desired ClientEvent types.
     *
     * @param jmClientApi Client API implementation
     */
    void initialize(final IClientAPI jmClientApi);

    /**
     * Used by JourneyMap to associate your mod id with your plugin instance.
     */
    String getModId();

    /**
     * Called by JourneyMap on the main Minecraft thread when a {@link ClientEvent} occurs.
     * Be careful to minimize the time spent in this method so you don't lag the game.
     * <p>
     * You must call {@link IClientAPI#subscribe(String, EnumSet)} to subscribe to these events ( preferably during
     * {@link #initialize(IClientAPI)} ), otherwise this method will never be called.
     * <p>
     * If the event type is {@link ClientEvent.Type#DISPLAY_UPDATE},
     * this is a signal to {@link IClientAPI#show(journeymap.client.api.display.Displayable)}
     * all relevant Displayables for the {@link ClientEvent#dimension} indicated.
     * (Note: ModWaypoints with persisted==true will already be shown.)
     *
     * @param event the event
     */
    void onEvent(final ClientEvent event);
}
