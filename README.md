# [JourneyMap for Minecraft][1]

Source code and build resources for [JourneyMap][2] ([http://journeymap.info][2])

## Requirements

* IntelliJ IDEA
* OpenJDK 1.8

## Environment Setup

### 1. Git the JourneyMap source

Check out a branch of the JourneyMap GIT repo to a directory called journeymap.  For example:

```sh
    git clone https://techbrew@bitbucket.org/TeamJM/journeymap.git
    cd journeymap
    git fetch && git checkout (branchname)
```

### 2. Setup JourneyMap with Forge for IntelliJ IDEA

* In a command window, go into the journeymap directory and invoke the Gradle build to setup the workspace:

```sh
    gradlew.bat setupDecompWorkspace
```

* Open IDEA. Import New Project > Browse to journeymap\build.gradle
* Check "Auto-import" and then press the Import / OK button.
* Close the Project when import is done.
* In a command window, invoke this Gradle task to create the Run configuration:

```sh
    gradlew.bat genIntellijRuns
```
* Open journeymap.ipr in IDEA
* Edit the "Minecraft Client" run configuration, add your credentials to `Program Arguments`: 

```
    --username name@email.address --password foo
```

### 3. Build the jars

* Update `project.properties` version info
* Build using Gradle (build.gradle) > build
* The end result will be in `build/libs/journeymap*.jar`

[1]: https://bitbucket.org/TeamJM/journeymap
[2]: http://journeymap.info