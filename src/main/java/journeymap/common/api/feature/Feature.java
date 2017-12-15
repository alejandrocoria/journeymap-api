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
package journeymap.common.api.feature;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * High-level JourneyMap features bundled in a marker interface,
 * grouped into categories by enum.
 */
public interface Feature
{
    /**
     * Player actions.
     */
    public enum Action implements Feature
    {
        /**
         * Player can teleport via /jtp.
         * Disabled by default for players.
         * Enabled by default for Ops.
         */
        Teleport(false, true);

        private boolean playerDefault;
        private boolean opDefault;

        private Action(boolean playerDefault, boolean opDefault)
        {
            this.playerDefault = playerDefault;
            this.opDefault = opDefault;
        }

        @Override
        public boolean isAllowedForPlayerByDefault()
        {
            return playerDefault;
        }

        @Override
        public boolean isAllowedForOpByDefault()
        {
            return opDefault;
        }
    }

    /**
     * Display interfaces in the client.
     * All enabled by default.
     */
    public enum Display implements Feature
    {
        /**
         * Compass/Heading UI (in-game).
         */
        Compass(false, true),
        /**
         * The Fullscreen map UI.
         */
        Fullscreen(true, false),
        /**
         * The Minimap UI (in-game).
         */
        Minimap(true, false),
        /**
         * Waypoint Beacons (in-game).
         */
        WaypointBeacon(false, true),
        /**
         * Waypoint Manager
         */
        WaypointManager(false, false),
        /**
         * The Webmap UI (via web browser).
         */
        Webmap(true, false);

        private boolean map;
        private boolean inGame;

        private Display(boolean isMap, boolean isInGame) {
            this.map = isMap;
            this.inGame = isInGame;
        }

        /**
         * Whether display shows map tiles.
         * @return
         */
        public boolean isMap()
        {
            return map;
        }

        /**
         * Whether display appears in-game.
         * @return
         */
        public boolean isInGame()
        {
            return inGame;
        }
    }

    /**
     * Types of maps generated in the client as the player explores.
     * All enabled by default.
     */
    public enum MapType implements Feature
    {
        /**
         * Surface in daylight map generation.
         */
        Day,
        /**
         * Surface at night map generation.
         */
        Night,
        /**
         * Underground (cave) map generation.
         */
        Underground,
        /**
         * Topographical (contour) map generation.
         */
        Topo,
        /**
         * Biome map generation.
         */
        Biome;
    }

    /**
     * Client display of entities within a Feature.Display (where applicable).
     * All enabled by default.
     */
    public enum Radar implements Feature
    {
        /**
         * Hostile mobs.
         */
        HostileMob,
        /**
         * Villagers, INpc, IMerchants.
         */
        NPC,
        /**
         * Passive mobs (animals).
         */
        PassiveMob,
        /**
         * Players.
         */
        Player,
        /**
         * Minecarts and Boats.
         */
        Vehicle,
        /**
         * Waypoints. Not technically an entity, but it's okay, really.
         */
        Waypoint;
    }

    String name();

    /**
     * Whether the Feature is allowed by default for players.
     * @return true if allowed
     */
    default boolean isAllowedForPlayerByDefault()
    {
        return true;
    }

    /**
     * Whether the Feature is allowed by default for Ops.
     * @return true if allowed
     */
    default boolean isAllowedForOpByDefault()
    {
        return true;
    }

    /**
     * Get the Feature type (enum class name).
     */
    default String getFeatureType()
    {
        return getClass().getSimpleName();
    }

    /**
     * Get the i18n key for the category of the Feature.
     * @return the key
     */
    default String getFeatureCategoryKey()
    {
        return String.format("jm.common.feature.%s", getFeatureType().toLowerCase());
    }

    /**
     * Get the i18n key for the Feature.
     * @return the key
     */
    default String getFeatureKey()
    {
        return String.format("%s.%s", getFeatureCategoryKey(), name().toLowerCase());
    }

    /**
     * Get the i18n key for the Feature tooltip.
     * @return the key
     */
    default String getFeatureTooltipKey()
    {
        return String.format("%s.tooltip", getFeatureKey());
    }
}
