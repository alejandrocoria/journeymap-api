# JourneyMap for Minecraft

## Requirements

* Java 6 or later
* Minecraft
* ModLoader : http://www.minecraftforum.net/topic/75440-v146-risugamis-mods-updated/
* Minecraft Coder Pack (MCP) : http://mcp.ocean-labs.de/index.php/Main_Page

## Environment Setup

* Patch your `minecraft.jar` with ModLoader per the ModLoader instructions
* Get a working MCP environment running using a ModLoader-patched `minecraft.jar`
* Run the MCP decompile script

## Download JourneyMap source

* Check out the JourneyMap GIT repo into your MCP directory.  For example:

    cd mcp
    git clone https://mwoodman@bitbucket.org/mwoodman/journeymap.git

## Compile in Eclipse (for development)

* Add the jars in mcp/lib to the eclipse/Client/.classpath

    `<classpathentry kind="lib" path="lib/servlet.jar"/>`
    `<classpathentry kind="lib" path="lib/servlet-2-3.jar"/>`
    `<classpathentry kind="lib" path="lib/war.jar"/>`
    `<classpathentry kind="lib" path="lib/webserver.jar"/>`
    `<classpathentry kind="lib" path="lib/YUIAnt.jar"/>`
    `<classpathentry kind="lib" path="lib/yuicompressor-2.4.6.jar"/>`

* Update JourneyMap source files as needed to compile with latest decompiled Minecraft + Modloader code

## Build the distributable JourneyMap.zip mod

### Checklist before you build

* Update `journeymap/build.xml` version info
* Update `src/minecraft/net/techbrew/mcjm/JourneyMap.java` version info
* Update `src/minecraft/net/techbrew/changelog.txt`

### Build using Ant

* The Ant build script will invoke MCP scripts and do the assembly magic:

    `ant -f journeymap/build.xml`

* The result will be in `journeymap/dist/JourneyMap*.zip`

### Test it in Minecraft

* Use a ModLoader-patched `minecraft.jar` in Minecraft
* Copy the `JourneyMap*.zip` to your `.minecraft/mods` folder
* Run Minecraft.  You should see an announcement in the chat window that JourneyMap is running.
