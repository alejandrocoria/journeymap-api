# [JourneyMap for Minecraft][1]

Source code and build resources for [JourneyMap][2] ([http://journeymap.info][2])

## Requirements

* Java 1.7 JDK
* IntelliJ IDEA

## Environment Setup

### 1. Git the JourneyMap source

Check out a branch of the JourneyMap GIT repo to a directory called journeymap.  For example:

```
    git clone https://techbrew@bitbucket.org/TeamJM/journeymap.git
    cd journeymap
    git fetch && git checkout 5.1.1_1.8
```

### 2. Setup JourneyMap with Forge for IntelliJ IDEA

* In a command window, go into the journeymap directory and invoke the Gradle build to setup the workspace:

```
    gradlew.bat setupDecompWorkspace
```

* Open journeymap.ipr in IDEA
* Import Project from Gradle when prompted
* Close the Project when import is done.
* In a command window, invoke this Gradle task to create the Run configuration:

```
    gradlew.bat genIntellijRuns
```
* Open journeymap.ipr in IDEA again
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