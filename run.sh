#!/usr/bin/env bash
echo "Starting ForeSight Inventory System..."
if command -v mvn &> /dev/null; then
    mvn compile exec:java -Dexec.mainClass="com.inventory.App"
else
    mkdir -p bin
    javac -encoding UTF-8 -cp "lib/*" -d bin src/main/java/com/inventory/*.java src/main/java/com/inventory/*/*.java
    java -cp "bin:lib/*" com.inventory.App
fi
