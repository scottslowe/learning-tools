#!/bin/bash

DAEMON_PATH=/opt/spark/sbin
DAEMON_NAME=spark
# Check that networking is up.
#[ ${NETWORKING} = "no" ] && exit 0

PATH=$PATH:$DAEMON_PATH

# See how we were called.
case "$1" in
  start)
        # Start daemon.
        echo "Starting $DAEMON_NAME";
        $DAEMON_PATH/start-all.sh
        ;;
  stop)
        # Stop daemons.
        echo "Stopping $DAEMON_NAME";
		$DAEMON_PATH/stop-all.sh
        ;;
  restart)
        $0 stop
        sleep 2
        $0 start
        ;;
  status)
        pid=`ps ax | grep -i 'spark' | grep -v grep | awk '{print $1}'`
        if [ -n "$pid" ]
          then
          echo "Spark is Running as PID: $pid"
        else
          echo "Spark is not Running"
        fi
        ;;
  *)
        echo "Usage: $0 {start|stop|restart|status}"
        exit 1
esac

exit 0
