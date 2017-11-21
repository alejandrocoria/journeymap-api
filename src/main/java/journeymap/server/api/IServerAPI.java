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

package journeymap.server.api;

import journeymap.common.api.IJmAPI;
import journeymap.common.api.feature.Feature;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

/**
 * Definition for the JourneyMap Server API. The runtime implementation is contained in the JourneyMap mod itself.
 *
 * Warning: Use of this API to put JourneyMap features behind a paywall (pay to play) is strictly forbidden and would
 * constitute a violation of the Minecraft EULA.
 */
@ParametersAreNonnullByDefault
public interface IServerAPI extends IJmAPI
{
    /**
     * This call can be used to enable or disable displays for a player in a specific dimension.
     *
     * @param player    The player.
     * @param dimension The dimension. 
     * @param displays  Set of displays to toggle.
     * @param enable    True to enable, false to disable.
     */
    void toggleDisplays(EntityPlayerMP player, int dimension, Set<Feature.Display> displays, boolean enable);

    /**
     * This call can be used to enable or disable displays for a player in a specific dimension.
     *
     * @param player    The player.
     * @param dimension The dimension. 
     * @param mapTypes  Set of MapTypes to toggle.
     * @param enable    True to enable, false to disable.
     */
    void toggleMapTypes(EntityPlayerMP player, int dimension, Set<Feature.MapType> mapTypes, boolean enable);

    /**
     * This call can be used to enable or disable Radar for a player in a specific dimension.
     *
     * @param player    The player.
     * @param dimension The dimension. 
     * @param radars    Set of Radars to toggle.
     * @param enable    True to enable, false to disable.
     */
    void toggleRadars(EntityPlayerMP player, int dimension, Set<Feature.Radar> radars, boolean enable);
}
