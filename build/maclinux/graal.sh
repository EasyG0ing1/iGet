rm -R $HOME/github/iget/graalvm
mkdir $HOME/github/iget/graalvm
java --enable-preview -agentlib:native-image-agent=config-output-dir=$HOME/github/iget/graalvm -jar $HOME/github/iget/target/iGet-jar-with-dependencies.jar setBrowser
java --enable-preview -agentlib:native-image-agent=config-merge-dir=$HOME/github/iget/graalvm -jar $HOME/github/iget/target/iGet-jar-with-dependencies.jar graal
java --enable-preview -agentlib:native-image-agent=config-merge-dir=$HOME/github/iget/graalvm -jar $HOME/github/iget/target/iGet-jar-with-dependencies.jar version
