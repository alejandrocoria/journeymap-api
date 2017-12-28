[JourneyMap API](https://bitbucket.org/TeamJM/journeymap-api) Changelog
======================================================

**API v2.0**

* Implemented in JourneyMap 5.6.0
* Java 8 required
* Server-side API added, Client-side API refactored, Waypoint structure refactored.

**API v1.9**

* Implemented in JourneyMap 5.5.0
* Java 8 required
* Waypoint-related refactoring and bugfixes
* Unified API for 1.9 through 1.12

**API v1.3**

* Java 8 required
* Support for Topo maps
* Experimental support for IClientAPI.requestMapTile()

**API v1.2**
* Minor documentation updates to stay in sync

**API v1.1**

* Basic support implemented in [JourneyMap 5.1.5 for Minecraft 1.8.9](http://minecraft.curseforge.com/projects/journeymap/files/2285371)
* Too many changes to be worth listing, since 1.0 wasn't ever implemented. Look at commits if you care to.

**API v1.0**

* Added constructor to Displayable that doesn't require displayId, added getGuid() and getDisplayType() methods, made several methods final.
* `IClientAPI.show()` now explicitly throws Exception. Added `IClientAPI.exists(Displayable)` overload, removed `IClientAPI.isVisible()`.
* Jars now available in Maven Central!
* Build of `journeymap-api-[version].jar` is still deobf, but no longer has 'deobf' in the name

**API v0.9**

* ModWaypoint now has an isEditable property
* Example Mod's `ExampleMapFacade.showBedWaypoint()` now takes a BlockPos
* Bugfix in Example Mod's SleepEventListener: event.result is always null
* Build updated to Forge 11.15.0.1712

**API v0.8**

* MapPoint has been replaced with Minecraft's BlockPos

**API v0.7**

* ClientEvent is now potentially cancellable.
* Added new ClientEvent.Type: DEATH_WAYPOINT

**API v0.6**

* Build now produces a `journeymap-api-*-deobf.jar` for use in a dev environment and a `journeymap-api-*-examplemod.jar` for use in the `run/mods` directory
* Method signature changes and bugfixes in PluginHelper

**API v0.5**

* Overhauled design to use the annotated plugin approach recommended by the Forge team.
* Updated dependency to Forge 1.8.9

**API v0.4 and earlier**

* Iterative designs, now obsolete