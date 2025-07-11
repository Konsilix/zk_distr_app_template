#!/bin/bash
set -e
#
# Set up the environment
#
APP="theApp"
ZK_NODE_IP=$(awk 'END{print $1}' /etc/hosts)
ZK_NODE_HOSTNAME=$(awk 'END{print $2}' /etc/hosts)
#ZK_NODE_HOSTNAME=${ZOO_MY_HOSTNAME}
APP_LOG=/logs/app.log
APP_JAR=/${APP}.jar
#RUN_CONFIG="-Dserver.port=3000 -Dmanagement.server.port=3001 -Dzk.url=${ZK_NODE_IP}:2181 -Dleader.algo=2"
#RUN_CONFIG="-Dzk.url=${ZK_NODE_IP}:2181 -Dleader.algo=2"
RUN_CONFIG="-Dzk.url=${ZK_NODE_HOSTNAME}:2181 -Dleader.algo=2"
ZK_LOG=/logs/zk.log
#
# Processes and Logging
#
echo "Container's hostname: $ZK_NODE_HOSTNAME" | tee -a ${ZK_LOG} ${APP_LOG}
echo "Container's IP address: $ZK_NODE_IP" | tee -a ${ZK_LOG} ${APP_LOG}
echo "Starting ZooKeeper Server" | tee -a ${ZK_LOG} ${APP_LOG}
echo "ZK ID = ${ZOO_MY_ID}" | tee -a ${ZK_LOG} ${APP_LOG}
echo ${ZOO_MY_ID} > /data/myid
#
# Start ZOOKEEPER
#
zkServer.sh start-foreground 2>&1 | tee -a ${ZK_LOG} &
sleep 10
# Start the App
echo "Starting App Server" | tee -a ${ZK_LOG} ${APP_LOG}
java ${RUN_CONFIG} -jar ${APP_JAR} 2>&1 >> ${APP_LOG}
# do not put the app in the background; otherwise, the container will exit.