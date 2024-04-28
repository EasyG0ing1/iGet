native-image \
-Djava.awt.headless=true \
--no-fallback \
--verbose \
--enable-preview \
--initialize-at-build-time=org.sqlite.util.ProcessRunner \
-cp "$HOME/.m2/repository/org/xerial/sqlite-jdbc/3.45.3.0/sqlite-jdbc-3.45.3.0.jar" \
-H:+ReportExceptionStackTraces \
-H:ReflectionConfigurationFiles=$HOME/github/iGet/graalvm/reflect-config.json \
-H:JNIConfigurationFiles=$HOME/github/iGet/graalvm/jni-config.json \
-H:ResourceConfigurationFiles=$HOME/github/iGet/graalvm/resource-config.json \
-H:SerializationConfigurationFiles=$HOME/github/iGet/graalvm/serialization-config.json \
-H:Name=$HOME/github/iGet/target/iget \
-jar $HOME/github/iGet/target/iGet-jar-with-dependencies.jar \
$HOME/github/iGet/target/iget
