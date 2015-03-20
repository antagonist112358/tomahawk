#!/bin/bash

# Set the sqoop variables
SQOOP_HOME=/usr/lib/gphd/sqoop
SQOOP_CONF_DIR=/etc/gphd/sqoop/conf

# Set the pig path
PIG_CONF_DIR=/etc/gphd/pig/conf

# Set the hadoop variables
export HADOOP_HOME=/usr/lib/gphd/hadoop
export HADOOP_CONF_DIR=/etc/gphd/hadoop/conf
HADOOP_CLIENT_HOME=/usr/lib/gphd/hadoop/client

# Set some variables for configuring the classpath
CORE_HADOOP=$HADOOP_CLIENT_HOME/*

SQOOP_DRIVERS=/usr/lib/gphd/sqoop/lib/db2jcc4.jar:/usr/lib/gphd/sqoop/lib/db2jcc.jar:/usr/lib/gphd/sqoop/lib/ojdbc6.jar:/usr/lib/gphd/sqoop/lib/ojdbc6.jar:/usr/lib/gphd/sqoop/lib/hsqldb-1.8.0.10.jar

APPLICATION_JAR=/usr/share/sbt-launcher-packaging/bin/sbt-launch.jar

# Set the entry point
ENTRY_POINT=xsbt.boot.Boot

# Group some of the path stuff together
CONFIG_DIRS=$SQOOP_CONF_DIR:$HADOOP_CONF_DIR:$PIG_CONF_DIR
JAR_DIRS=$CORE_HADOOP:$SQOOP_DRIVERS:$APPLICATION_JAR

export PIG_CLASSPATH=$CONFIG_DIRS

# Launch the application
echo Executing: java -Xmx16g -Xmx4g -cp "$CONFIG_DIRS:$JAR_DIRS" $ENTRY_POINT $@
                java -Xmx16g -Xmx4g -cp "$CONFIG_DIRS:$JAR_DIRS" $ENTRY_POINT $@
