@pushd %~dp0
@pushd ..\forge
gradlew.bat setupDecompWorkspace
gradlew.bat idea
@popd
@popd
@pause