# [JourneyMap for Minecraft][1]

Source code and build resources for [JourneyMap][2] ([http://journeymap.info][2])

## Requirements

* Java 1.7 JDK
* [Forge 1.7.10 or later][3]

## Environment Setup

### 1. Git the JourneyMap source

Check out a branch of the JourneyMap GIT repo to a directory called journeymap.  For example:

    git config core.ignorecase false
    git clone https://USERNAME@bitbucket.org/TeamJM/journeymap/branch/5.1.1_1.8 journeymap

### 2. Download Forge Source (http://files.minecraftforge.net/)

* Unzip the forge source zip to a directory called 'forge' as a sibling directory of journeymap
* Run journeymap/setupforge.bat (or setupforge.sh)
* Or manually:


```
#!dos

    cd forge
    gradlew.bat setupDecompWorkspace
    gradlew.bat idea
```


### 3. Update IDEA Project

* Open IDEA project created in new forge directory:  

    forge/forge.ipr
    
* Set the project SDK to a 1.7 JDK

    File > Project Structure... > Project > Project SDK

* Import the JourneyMap module file: 

    File > Import Module ... journeymap/journeymap.iml

* Update the Client run configuration to use JourneyMap's classpath

    Run > Edit Configurations > Minecraft Client > Use classpath of module: journeymap

## Build the distributable mod journeymap.jar

### 1. Update `build.xml` version info

### 2. Build using Ant from IDEA

Using IDEA, run the build.xml script. It will invoke the gradle scripts and do the assembly magic to package the mod 
into a .jar file for distribution.

The end result will be in `journeymap/dist/journeymap*.jar`

[1]: https://bitbucket.org/TeamJM/journeymap
[2]: http://journeymap.info
[3]: http://files.minecraftforge.net/