@echo off

mvn -f C:\github\iget\pom.xml clean -Pnative native:compile

rem native-image --no-fallback --verbose --enable-preview --initialize-at-build-time=org.sqlite.util.ProcessRunner -cp "%HOMEPATH%\.m2\repository\org\xerial\sqlite-jdbc\3.45.3.0\sqlite-jdbc-3.45.3.0.jar" -H:ReflectionConfigurationFiles=C:\github\iGet\graalvm\reflect-config.json -H:JNIConfigurationFiles=C:\github\iGet\graalvm\jni-config.json -H:ResourceConfigurationFiles=C:\github\iGet\graalvm\resource-config.json -H:SerializationConfigurationFiles=C:\github\iGet\graalvm\serialization-config.json -H:+ReportExceptionStackTraces -jar C:\github\iGet\target\iGet-jar-with-dependencies.jar C:\github\iGet\target\iget
