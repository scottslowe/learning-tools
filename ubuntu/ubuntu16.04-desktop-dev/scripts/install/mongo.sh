#!/bin/sh

install_mongo() {
	mkdir -p /home/vagrant/mongo-data
	
	docker run --name local-mongo -p 27017:27017 -v /home/vagrant/mongo-data:/data/db -d mongo

	if [ -e "/opt/mongobooster/mongobooster-4.1.2-x86_64.AppImage" ]; then return; fi

	mkdir -p /opt/mongobooster/
	
	curl -fsS http://s3.mongobooster.com/download/4.1/mongobooster-4.1.2-x86_64.AppImage -o mongobooster-4.1.2-x86_64.AppImage
	
	mv mongobooster-4.1.2-x86_64.AppImage /opt/mongobooster/
	
	chmod a+x /opt/mongobooster/mongobooster-4.1.2-x86_64.AppImage

	# Set desktop shortcut path
	DESK=/usr/share/applications/mongobooster.desktop

	# Add desktop shortcut
	echo "[Desktop Entry]\nEncoding=UTF-8\nName=Mongo Booster 4.1.2\nComment=Mongo Booster 4.1.2\nExec=/opt/mongobooster/mongobooster-4.1.2-x86_64.AppImage\nTerminal=false\nStartupNotify=true\nType=Application" -e > ${DESK}

	# Create symlink entry
	ln -s /opt/mongobooster/mongobooster-4.1.2-x86_64.AppImage /usr/local/bin/mongobooster
}