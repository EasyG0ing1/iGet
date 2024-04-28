## MacOS and Linux Build Environment

We can do all of this from a Terminal window, so open Terminal. You should be in your home folder. If not, type `cd ~`
and hit enter.

Next, install `SDKMan`

```bash
curl -s "https://get.sdkman.io" | bash
```

When it's done installing, close Terminal then re-open it and make sure you're in your user home folder.

Next, install graalvm

```bash
sdk install java 22.0.1-graal
```

This will take a little time to complete but once its done, your environment for graalvm / Java 22 will already be
configured for you.

Next, you need to install maven.

```bash
mkdir ~/maven
cd ~/maven
wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip
unzip apache-maven-3.9.6-bin.zip
mv apache-maven-3.9.6 extracted
export PATH="~/maven/extracted/bin":$PATH
```

Next, create a folder called github

```bash
mkdir ~/github
cd ~/github
```

Now type `mvn --version` and hit enter and you should see something that looks like this:

```bash
Apache Maven 3.9.6 (bc0240f3c744dd6b6ec2920b3cd08dcc295161ae)
(path to Maven home:)
Java version: 22.0.1, vendor: BellSoft, runtime: /Library/Java/LibericaNativeImageKit/liberica-vm-full-24.0.1-openjdk22/Contents/Home
Default locale: en_US, platform encoding: UTF-8
OS name: "mac os x", version: "14.4.1", arch: "x86_64", family: "mac"
```

Go back to the [Main Instructions](./environment.md) and read from the section entitled **First**
