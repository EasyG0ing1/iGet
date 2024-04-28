rm -R $HOME/github/iGet/graalvm
mkdir $HOME/github/iGet/graalvm
java --enable-preview -agentlib:native-image-agent=config-output-dir=$HOME/github/iGet/graalvm -jar $HOME/github/iGet/target/iGet-jar-with-dependencies.jar setBrowser
java --enable-preview -agentlib:native-image-agent=config-merge-dir=$HOME/github/iGet/graalvm -jar $HOME/github/iGet/target/iGet-jar-with-dependencies.jar graal
