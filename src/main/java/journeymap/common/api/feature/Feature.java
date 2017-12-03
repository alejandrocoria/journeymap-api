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
         * Player can teleport via /jtp
         */
        Teleport;
    }

    /**
     * Display interfaces in the client.
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
}
