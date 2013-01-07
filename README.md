# JourneyMap for Minecraft

## Requirements

* Java 6 or later
* Minecraft
* ModLoader : http://www.minecraftforum.net/topic/75440-v146-risugamis-mods-updated/
* Minecraft Coder Pack (MCP) : http://mcp.ocean-labs.de/index.php/Main_Page

## Environment Setup

### 1. Download JourneyMap source

Check out the JourneyMap GIT repo to what will become your MCP workspace.  For example:

    git clone https://mwoodman@bitbucket.org/mwoodman/journeymap.git mcp
    
Note: The 'mcp' directory name at the end of the git clone command is just to avoid having 'journeymap/journeymap' in your project structure, and serves as a reminder where the MCP files need to go.
    
### 2. Patch Minecraft with Modloader

* Patch your `minecraft.jar` with ModLoader per the ModLoader instructions

### 3. Setup MCP

* Download and unzip the MCP zip.  
* Put the contents of the MCP root directory (like mcp795) into the `mcp` directory where you cloned the Git repo
* Copy the minecraft jars in `mcp/jars` per the usual MCP instructions
* Run `mcp/update.bat' (or .sh)
* Run `mcp/decompile.bat' (or .sh)

### 4. Update Eclipse Project

Add the jars in `mcp/lib` to the `eclipse/Client/.classpath` :

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
