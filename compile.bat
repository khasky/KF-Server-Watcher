@echo off
cls
set jarname=KFServerWatcher-v0.3.8.jar
set configfile=kfsw.ini
set runfile=run.bat
if exist build (
	cd build
	del /s /q /f *.jar
	del /s /q /f *.ini
	cd ../src/com/khasky
) else (
	cd src/com/khasky
)
del /s /q /f *.class
cd ../..
echo Compiling java classes
javac com/khasky/KFServerWatcher.java
echo Creating JAR file
jar -cfmv %jarname% manifest.txt com/khasky/*.class
echo Moving files
cd ..
mkdir build
cd src
move %jarname% ../build
cd ..
copy %configfile% build\%configfile%
copy %runfile% build\%runfile%
pause