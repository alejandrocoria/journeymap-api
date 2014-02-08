# [JourneyMap for Minecraft][1]

Source code and build resources for [JourneyMap][2] ([http://journeymap.techbrew.net][2])

## Requirements

* Java 6 or later
* [Forge 1.7.2 or later][3]

## Environment Setup

### 1. Git the JourneyMap source

Check out the JourneyMap GIT repo to a directory called journeymap.  For example:

    git config core.ignorecase false
    git clone https://mwoodman@bitbucket.org/mwoodman/journeymap.git

### 2. Download Forge Source

* run "gradlew.bat setupDecompWorkspace"
* run "gradlew.bat idea"

### 3. Update IDEA Project

* Open IDEA project created in new forge directory
* Import the JourneyMap module
* Update the Client run configuration to use JourneyMap's classpath

## Build the distributable JourneyMap.zip mod

### 1. Checklist before you build

* Update `build.xml` version info
* Update `resources/changelog.txt` version info and details

### 2. Build using Ant from IDEA

Using IDEA, run the build.xml script.  It will invoke the gradle scripts and do the assembly magic to package the mod into a .jar file for distrobution.

The end result will be in `journeymap/dist/JourneyMap*.zip`

[1]: https://bitbucket.org/mwoodman/journeymap
[2]: http://journeymap.techbrew.net
[3]: http://files.minecraftforge.net/

