# Building and running

    $ cd hadoop/wordcount-hdfs-copy
    $ mvn clean package appassembler:assemble
    $ sh ./target/appassembler/bin/streaming

To enable syslog to publish to the ports used in the example:

  For rsyslog we can use tcp (Fedora, Ubuntu, Mint):
    modify (using sudo): /etc/rsyslog.conf OR /etc/rsyslog.d/50-default.conf
    add this line at the end:
    
        local3.*	@@127.0.0.1:1514

    restart rsyslog:
      $ sudo service rsyslog restart
  
  For syslog we need to use udp (Mac OS X):
    modify (using sudo): /etc/syslog.conf
    add this line at the end:
    
        local3.*	@127.0.0.1:1513

    restart syslogd:
      $ ps -e | grep syslogd
      14227 ?? 0:00.03 /usr/sbin/syslogd
      14420 ttys005 0:00.00 grep syslogd
      $ sudo kill -HUP 14227

    change streaming/src/main/resources/META-INF/spring/application-context.xml to use udp channel adapter:

      <int-ip:udp-inbound-channel-adapter id="udpAdapter" 
          channel="syslogChannel" port="${syslog.udp.port}" />

To send a message to syslog

    $ logger -p local3.info -t TESTING "Test Syslog Message"

Look at the data inside hadoop

    $ hadoop fs -ls /data
    $ hadoop fs -cat /data/syslog-0.log

