#!/bin/sh

install_softether-vpn() {

	if [ -e "/opt/softether-vpn" ]; then return; fi

	mkdir -p /opt/softether-vpn/
	
	curl -fsS http://www.softether-download.com/files/softether/v4.24-9651-beta-2017.10.23-tree/Linux/SoftEther_VPN_Client/64bit_-_Intel_x64_or_AMD64/softether-vpnclient-v4.24-9651-beta-2017.10.23-linux-x64-64bit.tar.gz -o softether-vpnclient-v4.24-9651-beta-2017.10.23-linux-x64-64bit.tar.gz
	
	tar -xvf softether-vpnclient-v4.24-9651-beta-2017.10.23-linux-x64-64bit.tar.gz

	mv vpnclient /opt/softether-vpn/

	make -C '/opt/softether-vpn/vpnclient' i_read_and_agree_the_license_agreement

	mv /tmp/vagrant/scripts/resources/vpnclient.sh > /opt/softether-vpn/vpnclient/
	mv /tmp/vagrant/scripts/resources/vpncmd.sh > /opt/softether-vpn/vpnclient/
	
	chmod +x /opt/softether-vpn/vpnclient/vpnclient.sh
	chmod +x /opt/softether-vpn/vpnclient/vpncmd.sh

	# Create symlink entry
	ln -s /opt/softether-vpn/vpnclient/vpnclient.sh /usr/local/bin/vpnclient
	ln -s /opt/softether-vpn/vpnclient/vpncmd.sh /usr/local/bin/vpncmd
}