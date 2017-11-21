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

package journeymap.server.api.impl;

import com.google.common.base.Joiner;
import journeymap.common.api.feature.Feature;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

/**
 * Stub implementation of the IServerAPI. Doesn't actually do anything except log statements.
 */
@Optional.Interface(iface = "journeymap.server.api.IServerAPI", modid = "journeymap")
@ParametersAreNonnullByDefault
enum MockServerAPI implements journeymap.server.api.IServerAPI
{
    INSTANCE;

    private final static Logger LOGGER = LogManager.getLogger("journeymap-stub");

    @Override
    public void toggleDisplays(EntityPlayerMP player, int dimension, Set<Feature.Display> displays, boolean enable)
    {
        log(String.format("Mock toggled displays (%s) for %s in dim %s: %s", enable, player, dimension, Joiner.on(",").join(displays)));
    }

    @Override
    public void toggleMapTypes(EntityPlayerMP player, int dimension, Set<Feature.MapType> mapTypes, boolean enable)
    {
        log(String.format("Mock toggled mapTypes (%s) for %s in dim %s: %s", enable, player, dimension, Joiner.on(",").join(mapTypes)));
    }

    @Override
    public void toggleRadars(EntityPlayerMP player, int dimension, Set<Feature.Radar> radars, boolean enable)
    {
        log(String.format("Mock toggled radars (%s) for %s in dim %s: %s", enable, player, dimension, Joiner.on(",").join(radars)));
    }

    /**
     * Log what's happening in the stub, since there's nothing real to see.
     *
     * @param message
     */
    private void log(String message)
    {
        LOGGER.info(String.format("[%s] %s", getClass().getSimpleName(), message));
    }
}
