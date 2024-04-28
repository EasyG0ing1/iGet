native-image \
-Djava.awt.headless=true \
--no-fallback \
--verbose \
--enable-preview \
--initialize-at-build-time=org.sqlite.util.ProcessRunner \
-cp "$HOME/.m2/repository/org/xerial/sqlite-jdbc/3.45.3.0/sqlite-jdbc-3.45.3.0.jar" \
-H:+ReportExceptionStackTraces \
-H:ReflectionConfigurationFiles=$HOME/github/iget/graalvm/reflect-config.json \
-H:JNIConfigurationFiles=$HOME/github/iget/graalvm/jni-config.json \
-H:ResourceConfigurationFiles=$HOME/github/iget/graalvm/resource-config.json \
-H:SerializationConfigurationFiles=$HOME/github/iget/graalvm/serialization-config.json \
-H:Name=$HOME/github/iget/target/iget \
-jar $HOME/github/iget/target/iGet-jar-with-dependencies.jar \
$HOME/github/iget/target/iget
