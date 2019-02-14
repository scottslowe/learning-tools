#!/bin/bash

install_gradle() {

	if [ -e "/opt/gradle-4.3.1" ]; then return; fi

	wget "https://services.gradle.org/distributions/gradle-4.3.1-all.zip" --referer=https://services.gradle.org

	mkdir -p /opt/gradle

	unzip gradle-4.3.1-all.zip -d /opt/

	ln -s /opt/gradle-4.3.1/bin/gradle /usr/local/bin/gradle
}
