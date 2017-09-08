[JourneyMap API](https://bitbucket.org/TeamJM/journeymap-api)
============================================================

A plugin-style (soft dependency) API allowing other mods to create waypoints and show overlays and markers 
within [JourneyMap for Minecraft](http://journeymap.info) 1.8.9+.

If you have suggestions or improvements to the API structure, feel free to make Pull Requests. Chatting with the TeamJM
developers in Espernet IRC #journeymap is highly suggested.  Before you change anything or submit code, however, be sure
to read the [License Information](docs/license.md).

News
============================================================

* **8 Sep 2017**: Version 1.4 of the API for Minecraft versions 1.9, 1.9.4, 1.10.2, 1.11.2, and 1.12 has been pushed to Maven. It is implemented in JourneyMap 5.5.0b2 and above.

* **22 Nov 2016**: Version 1.11-1.3-SNAPSHOT has been pushed to Maven

* **22 Nov 2016**: Version 1.9.4-1.3 and 1.10.2-1.3 have been pushed to Maven

* **27 May 2016**: Version 1.9-1.2 and 1.9.4-1.2 have been pushed to Maven

* **26 May 2016**: Version 1.8.9-1.2 has been pushed to Maven

* **18 March 2016**: Version 1.9-1.2-SNAPSHOT has been pushed to Maven

* **4 March 2016**: Version 1.8.9-1.1 of the API is in Maven, has basic support implemented in [JourneyMap 5.1.5 for Minecraft 1.8.9](http://minecraft.curseforge.com/projects/journeymap-32274/files/2285371).

* **25 Feb 2016**: Version 1.1 of the API design is complete.

* **26 Jan 2016**: Version 1.0 of the API design is complete, and Techbrew has begun writing the implementation for it in JourneyMap. Watch
this site or follow @JourneyMapMod on Twitter for news about releases that support this API.

* **25 Jan 2016**: JourneyMap API artifacts are now hosted in [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cjourneymap-api)


[How to use the JourneyMap API](docs/howto.md)
============================================================

*Write a plugin for JourneyMap you can add to your mod.*


[Change Log](docs/changelog.md)
============================================================

*See what's changed across API versions.*


[License Information](docs/license.md)
============================================================

*Who owns this code and what can you do with it.*


Help Wanted
============================================================

**Areas of help needed:**

* Unit tests!
* Utility classes utilizing [java.awt.geom.Area.add()](https://docs.oracle.com/javase/7/docs/api/java/awt/geom/Area.html) to 
create optimized polygons comprised of multiple chunks.