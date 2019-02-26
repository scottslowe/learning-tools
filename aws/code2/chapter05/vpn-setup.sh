#!/bin/bash -ex

#param IPSEC_PSK the shared secret
#param VPN_USER the vpn username
#param VPN_PASSWORD the vpn password
 
PRIVATE_IP="$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4)"
PUBLIC_IP="$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)"

yum-config-manager --enable epel
yum clean all
yum install -y openswan xl2tpd

cat > /etc/ipsec.conf <<EOF
version 2.0
 
config setup
	nat_traversal=yes
	virtual_private=%v4:10.0.0.0/8,%v4:192.168.0.0/16,%v4:172.16.0.0/12,%v4:25.0.0.0/8,%v6:fd00::/8,%v6:fe80::/10
	oe=off
	protostack=netkey
	nhelpers=0
	interfaces=%defaultroute

conn vpnpsk
	auto=add
	left=$PRIVATE_IP
	leftid=$PUBLIC_IP
	leftsubnet=$PRIVATE_IP/32
	leftnexthop=%defaultroute
	leftprotoport=17/1701
	rightprotoport=17/%any
	right=%any
	rightsubnetwithin=0.0.0.0/0
	forceencaps=yes
	authby=secret
	pfs=no
	type=transport
	auth=esp
	ike=3des-sha1
	phase2alg=3des-sha1
	dpddelay=30
	dpdtimeout=120
	dpdaction=clear
EOF
 
cat > /etc/ipsec.secrets <<EOF
$PUBLIC_IP %any : PSK "${IPSEC_PSK}"
EOF
 
cat > /etc/xl2tpd/xl2tpd.conf <<EOF
[global]
port = 1701

[lns default]
ip range = 192.168.42.10-192.168.42.250
local ip = 192.168.42.1
require chap = yes
refuse pap = yes
require authentication = yes
name = l2tpd
pppoptfile = /etc/ppp/options.xl2tpd
length bit = yes
EOF

cat > /etc/ppp/chap-secrets <<EOF
${VPN_USER} l2tpd ${VPN_PASSWORD} *
EOF

cat > /etc/ppp/options.xl2tpd <<EOF
ipcp-accept-local
ipcp-accept-remote
ms-dns 8.8.8.8
ms-dns 8.8.4.4
noccp
auth
crtscts
idle 1800
mtu 1280
mru 1280
lock
connect-delay 5000
EOF
 
iptables -t nat -A POSTROUTING -s 192.168.42.0/24 -o eth0 -j MASQUERADE
echo 1 > /proc/sys/net/ipv4/ip_forward
iptables-save > /etc/iptables.rules

mkdir -p /etc/network/if-pre-up.d
cat > /etc/network/if-pre-up.d/iptablesload <<EOF
#!/bin/sh
iptables-restore < /etc/iptables.rules
echo 1 > /proc/sys/net/ipv4/ip_forward
exit 0
EOF

service ipsec start
service xl2tpd start

chkconfig ipsec on
chkconfig xl2tpd on
