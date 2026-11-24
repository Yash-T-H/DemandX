@echo off
echo Starting ForeSight Inventory System...
mvn compile exec:java -Dexec.mainClass="com.inventory.App"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Running without Maven if libraries are present in lib/...
    if not exist "bin" mkdir bin
    javac -encoding UTF-8 -cp "lib\*" -d bin src\main\java\com\inventory\*.java src\main\java\com\inventory\*\*.java
    java -cp "bin;lib\*" com.inventory.App
    pause
)
