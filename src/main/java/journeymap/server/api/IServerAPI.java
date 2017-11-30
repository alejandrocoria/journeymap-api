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

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.UUID;

/**
 * Definition for the JourneyMap Server API. The runtime implementation is contained in the JourneyMap mod itself.
 *
 * Warning: Use of this API to put JourneyMap features behind a paywall (pay to play) is strictly forbidden and
 * such use would be a violation of the Minecraft EULA.
 */
@ParametersAreNonnullByDefault
public interface IServerAPI extends IJmAPI
{
    /**
     * Set features to be enabled/disabled for a player in a specific dimension.
     *
     * @param modId       Mod ID making the change
     * @param playerID    The player UUID.
     * @param dimension   The dimension.
     * @param featureMap  A map of features with booleans indicating whether they are enabled/disabled.
     */
    void setPlayerFeatures(String modId, UUID playerID, int dimension, Map<Enum<? extends Feature>, Boolean> featureMap);

    /**
     * Get the current map of features for a player in a specific dimension.
     * @param playerID    The player UUID.
     * @param dimension   The dimension.
     * @return A map of features with booleans indicating whether they are enabled/disabled.
     */
    Map<Enum<? extends Feature>, Boolean> getPlayerFeatures(UUID playerID, int dimension);

    /**
     * Get the default server-configured features for the dimension.
     * @param dimension the dim
     * @param isOp if true, features for Ops, otherwise for normal players.
     * @return A map of features with booleans indicating whether they are enabled/disabled.
     */
    Map<Enum<? extends Feature>, Boolean> getServerFeatures(int dimension, boolean isOp);

}
