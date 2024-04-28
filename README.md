# Build Tools

I build the native binaries using graalVM

And because of the uniqueness of each operating system and the JNI reflection calls, each operating system has to be
built in a very specific way, where we generate the graal configuration json files independently for each OS before
compiling to a native-image.

This process is better suited for those who have built native-images before, but if you think you can handle it, I'll
try and explain this in as much detail as I can for you.

Click on your operating system to see how to set up the build environment:

[Windows](./Windows.md)

[MacOS](./MacLinux.md)

[Linux](./MacLinux.md)

## First

Clone the repository to your local drive:

```bash
git clone https://github.com/EasyG0ing1/iget.git
cd iGet
```

* If you don't have the git command, Windows users [click here](./gitwindows.md)

* Mac and Linux users just need to instaall via your package manager

  ```bash
  sudo apt install git -y (linux)
  brew install git (mac)
  ```

### MacOS or Linux

```bash
cd ~/github/iGet/build/maclinux
chmod +x *.sh
./buildjar.sh
```

### Windows

```bash
cd C:\github\iGet\build\windows
buildjar.bat
```

This should create the file under `iGet/target` called `iGet-jar-with-dependencies.jar`

This next step is heavily reliant on which browser you use to access Instagram.

This step will run the program under a GraalVM monitor that will watch the execution of the program so that it can build
the necessary json config files that are specific to your operating system. This means that we will need to be able to
run the program with the `watch` option, then when we copy an instagram reel link to your clipboard, you will need to
see that download happen successfully.

For Windows, I recommend using Chrome, but only AFTER you have modified the shortcut as described in
the [readme](../README.md) under the *Operating System Notes*.

For Linux / Ubuntu, you should be using Firefox

For MacOS, using Chrome will work fine.

Launch your browser and go to instagram.com and log into your Instagram account. Next, find a reel. If clicking on reel
on the left side of the screen doesn't work for you, then just find a content creator and from their page, click on
Reels then click on one of their reels.

After that, go into your terminal (cmd window) and type:

### Windows

```bash
graal.bat
```

### MacOS or Linux

```bash
./graal.sh
```

You will see it do some things, but then it will ask you which browser you're using. type 1 for Chrome if that's what
you're using. If you are using Firefox, then type 4 and hit enter. It will then ask you if you want it to locate your
history file for you, just type `Y` and hit enter.

The next thing you should see will look like this:

```bash
Watching Clipboard - Ctrl+C to exit
```

And **THAT'S ALL YOU SHOULD SEE**. If you see a command prompt show up after that, then something went wrong. Open an
issue and lets talk about it.

Next, go to your browser and highlight all of the text in the url field, then RIGHT CLICK on it and select copy. The
text should look something like this:

```azure
https://www.instagram.com/reel/C6RT4CKyl2O/
```

Once you copy it to your clipboard, you should see in the terminal something like this:

```azure
Watching Clipboard - Ctrl+C to exit
Downloading: https://www.instagram.com/reel/C6RT4CKyl2O/?utm_source=ig_embed
Success: https://www.instagram.com/reel/C6RT4CKyl2O/?utm_source=ig_embed
```

If the Downloading line doesn't show up right away for some reason, click on the Terminal window to make it active, then
click on the web browser to make it active and copy the text to the clipboard again.

It might take a few seconds to download. After you see `Success`, click in the Terminal window and hit `ctrl+c` to stop
the program.

Next, type:

### Windows

```bash
native.bat
```

### MacOS / Linux

```bash
./native.sh
```

Now, this is going to build the native binary and IT WILL TAKE SOME TIME, depending on how much RAM you have and how
fast your CPU is. On a Macbook Pro 2019 with 32 gigs of ram, the process takes just over a minute. In a virtual machine
on that same laptop, it takes just under two minutes. But on a slower ESXi server that I have it can take almost seven
minutes ... just be patient!

Once it is finished, you should find the binary in the `iGet/target` folder and it will be named `iget.exe` in Windows
or just `iget` in Mac / Linux.

You will know that the binary works when you can run it with the `watch` option

```bash
iGet/target/iget.exe watch
iGet/target/iget watch
```

and you should only see this line without being kicked back to a command prompt:

```bash
Watching Clipboard - Ctrl+C to exit
```

Move the binary to a folder that is in your PATH environment variable so that it will run from anywhere you type the
command.

# CLEANUP

## Windows

You can cleanup your build environment by reversing everything we did when we set it up. Start with removing the
environment variables you created. If you need to look at what those were, [click here](./Windows.md). Then you can
remove graal, maven, and the github clone / source files by simply typing:

```bash
cd C:\
rd /s /q C:\graalvm
rd /s /q C:\github
```

Then you can delete the zip files you downloaded in your Downloads folder.

## MacOS and Linux

This process is a little more involved than it is for Windows, but certainly dooable.
Start by uninstalling graalvm. It SHOULD uninstall by typing

```bash
sdk uninstall java 22.0.1-graal
```

If that doesn't work, then list what's installed by typing:

```bash
sdk list java
```

It will spit out a list of a bunch of javas that are available for installation, but only one of them should be marked
as `installed` under the `Status` column. The text in the `Identifier` column is what you want to copy. So lets say that
the next to the word `Installed` in the `Status` column, you see the text `22-graal` in the `Identifier` column. Just
type

```bash
sdk uninstall java 22-graal
```

After we've uninstalled graal, we can uninstall sdkman but it requires some manual removal of things. In
your `~/.bashrc` or your `~/.bash_profile` file, there will be some lines we need to delete, so open it:

```bash
nano ~/.bashrc
nano ~/.bash_profile
```

and look for some lines that look like this:

```bash
#THIS MUST BE AT THE END OF THE FILE FOR SDKMAN TO WORK!!!
export SDKMAN_DIR="$HOME/.sdkman"
[[ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "$HOME/.sdkman/bin/sdkman-init.sh"
```

Delete them - the fastest way to delete a line in nano is to move the cursor to that line then hit `ctrl+k`. Once those
lines are deleted, save and exit by typing `ctrl+x` then `y` then `<enter>`

Next, you can remove the relevant folders:

```bash
rm -R ~/.sdkman
rm -R ~/maven
rm -R ~/github
```

Close Terminal and open it back up.

Then make sure there aren't any rogue environment variables being created by typing:

```bash
env | grep SDKMAN
```

If you see any, you'll have to search through your files that start with `.bash` and see if you can find where those
environment variables are being created then just remove them from those files and re-open your terminal window again
and you should be good to go.
