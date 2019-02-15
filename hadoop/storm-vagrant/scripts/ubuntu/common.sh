apt-get update -y

apt-get install -y software-properties-common python-software-properties

add-apt-repository -y ppa:openjdk-r/ppa

apt-get update -y

apt-get install -y openjdk-8-jdk

echo export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 >> /etc/environment

source /etc/environment
