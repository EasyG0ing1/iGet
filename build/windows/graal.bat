@echo off
rd /s /q C:\github\iGet\graalvm
rd /s /q %APPDATA%\iGet
mkdir C:\github\iGet\graalvm
java --enable-preview -agentlib:native-image-agent=config-output-dir=C:\github\iGet\graalvm -jar C:\github\iGet\target\iGet-jar-with-dependencies.jar setBrowser
java --enable-preview -agentlib:native-image-agent=config-merge-dir=C:\github\iGet\graalvm -jar C:\github\iGet\target\iGet-jar-with-dependencies.jar graal
java --enable-preview -agentlib:native-image-agent=config-merge-dir=C:\github\iGet\graalvm -jar C:\github\iGet\target\iGet-jar-with-dependencies.jar version
