# [JourneyMap for Minecraft][1]

Source code and build resources for [JourneyMap][2] ([http://journeymap.techbrew.net])

## Requirements

* Java 6 or later
* [Minecraft][3]
* [ModLoader][4] (Must match the version of Minecraft you have)
* [Minecraft Coder Pack (MCP)][5] (Must match the version of Minecraft you have)

## Environment Setup

### 1. Git the JourneyMap source

Check out the JourneyMap GIT repo to what will become your MCP workspace.  For example:

    git clone https://mwoodman@bitbucket.org/mwoodman/journeymap.git mcp
    
Note: The 'mcp' directory name at the end of the git clone command is just to avoid having 'journeymap/journeymap' in your project structure, and serves as a reminder where the MCP files need to go.
    
### 2. Patch Minecraft with Modloader

* Patch your `minecraft.jar` with ModLoader per ModLoader's instructions

### 3. Setup MCP

* Download and unzip the MCP zip.  
* Put the contents of the MCP root directory (like mcp795) into the `mcp` directory where you cloned the Git repo
* Copy the minecraft jars in `mcp/jars` per the usual MCP instructions
* Run `mcp/update.bat' (or .sh)
* Run `mcp/decompile.bat' (or .sh)

### 4. Update Eclipse Project

Add the jars in `mcp/lib` to the Eclipse client project.  You can either use Eclipse to modify the build path, or you can add these lines in the `eclipse/Client/.classpath` file:

    <classpathentry kind="lib" path="lib/servlet.jar"/>
    <classpathentry kind="lib" path="lib/servlet-2-3.jar"/>
    <classpathentry kind="lib" path="lib/war.jar"/>
    <classpathentry kind="lib" path="lib/webserver.jar"/>
    <classpathentry kind="lib" path="lib/YUIAnt.jar"/>
    <classpathentry kind="lib" path="lib/yuicompressor-2.4.6.jar"/>

## Build the distributable JourneyMap.zip mod

### 1. Checklist before you build

* Update `journeymap/build.xml` version info
* Update `src/minecraft/net/techbrew/mcjm/JourneyMap.java` version info
* Update `src/minecraft/net/techbrew/changelog.txt`

### 2. Build using Ant

The Ant build script will invoke MCP scripts and do the assembly magic:

    ant -f journeymap/build.xml

The result will be in `journeymap/dist/JourneyMap*.zip`

### 3. Test it in Minecraft

* Use a ModLoader-patched `minecraft.jar` in Minecraft
* Copy the `JourneyMap*.zip` to your `.minecraft/mods` folder
* Run Minecraft.  You should see an announcement in the chat window that JourneyMap is running.

[1]: https://bitbucket.org/mwoodman/journeymap
[2]: http://journeymap.techbrew.net
[3]: http://minecraft.net
[4]: http://www.minecraftforum.net/topic/75440-v146-risugamis-mods-updated/
[5]: http://mcp.ocean-labs.de/index.php/Main_Page
