#!/bin/sh

install_intellij-idea() {

	if [ -e "/usr/local/bin/idea" ]; then return; fi

	echo "Installing IntelliJ IDEA..."
	#[ $(id -u) != "0" ] && exec sudo "$0" "$@"

	# Attempt to install a JDK
	# apt-get install openjdk-8-jdk
	# add-apt-repository ppa:webupd8team/java && apt-get update && apt-get install oracle-java8-installer

	# Prompt for edition
	#while true; do
	#    read -p "Enter 'U' for Ultimate or 'C' for Community: " ed 
	#    case $ed in
	#        [Uu]* ) ed=U; break;;
	#        [Cc]* ) ed=C; break;;
	#    esac
	#done
	ed=C

	# Fetch the most recent version
	VERSION=$(wget "https://www.jetbrains.com/intellij-repository/releases" --referer=https://www.jetbrains.com  -qO- | grep -P -o -m 1 "(?<=https://www.jetbrains.com/intellij-repository/releases/com/jetbrains/intellij/idea/BUILD/)[^/]+(?=/)")

	# Prepend base URL for download
	URL="https://download.jetbrains.com/idea/ideaI$ed-$VERSION.tar.gz"

	echo $URL

	# Truncate filename
	FILE=$(basename ${URL})

	# Create downloads folder
	mkdir ~/Downloads

	# Set download directory
	DEST=~/Downloads/$FILE

	echo "Downloading idea-I$ed-$VERSION to $DEST..."

	# Download binary
	wget -cO ${DEST} ${URL} --read-timeout=5 --tries=0

	echo "Download complete!"

	# Set directory name
	DIR="/opt/idea-I$ed-$VERSION"

	echo "Installing to $DIR"

	# Untar file
	if mkdir ${DIR}; then
	    tar -xzf ${DEST} -C ${DIR} --strip-components=1
	fi

	# Grab executable folder
	BIN="$DIR/bin"

	# Add permissions to install directory
	chmod -R +rwx ${DIR}

	# Set desktop shortcut path
	DESK=/usr/share/applications/IDEA.desktop

	# Add desktop shortcut
	echo "[Desktop Entry]\nEncoding=UTF-8\nName=IntelliJ IDEA\nComment=IntelliJ IDEA\nExec=${BIN}/idea.sh\nIcon=${BIN}/idea.png\nTerminal=false\nStartupNotify=true\nType=Application" -e > ${DESK}

	# Create symlink entry
	ln -s ${BIN}/idea.sh /usr/local/bin/idea

	echo "Done."  
}