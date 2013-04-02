# [JourneyMap for Minecraft][1]

Source code and build resources for [JourneyMap][2] ([http://journeymap.techbrew.net][2])

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

* Open Eclipse using `mcp/eclipse` as your workspace.
* Open the Client Project and select "Build Path -> Configure Build Path ..."
* In the Libraries section, add the External JARS located in `mcp/lib`.

## Build the distributable JourneyMap.zip mod

### 1. Checklist before you build

* Update `eclipse/Client/build.xml` version info
* Update `src/minecraft/net/techbrew/mcjm/JourneyMap.java` version info
* Update `src/minecraft/net/techbrew/changelog.txt`

### 2. Build using Ant from Eclipse

Using Eclipse, run the build.xml script in the Client project.  It will invoke the MCP recompile scripts and do the assembly magic to package the mod into a .zip file for distrobution.

The end result will be in `journeymap/dist/JourneyMap*.zip`

### 3. Test it in Minecraft

* Use a ModLoader-patched `minecraft.jar` in Minecraft
* Copy the `JourneyMap*.zip` to your `.minecraft/mods` folder
* Run Minecraft.  You should see an announcement in the chat window that JourneyMap is running.

## Recover files after running MCP clean or update scrips

* First, stage+commit+push everything that has been changed
* If you're running Eclipse, it might be good to shut it down to prevent it from keeping file handles
* Run MCP clean/update script (either will delete the src directory)
* Git reset your branch to HEAD using the Git GUI (Branch menu -> Reset) or:

    git reset --hard HEAD^


[1]: https://bitbucket.org/mwoodman/journeymap
[2]: http://journeymap.techbrew.net
[3]: http://minecraft.net
[4]: http://www.minecraftforum.net/topic/75440-v146-risugamis-mods-updated/
[5]: http://mcp.ocean-labs.de/index.php/Main_Page
