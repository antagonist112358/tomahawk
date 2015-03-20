#!/bin/bash

# Find the directory of the script
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Assume the script is in the "PROJECT_DIR/bin" directory
PROJECT_DIR="$( cd "$( dirname "${DIR}../" )" && pwd )"

# Set the sqoop variables
SQOOP_HOME=/usr/lib/gphd/sqoop
SQOOP_CONF_DIR=/etc/gphd/sqoop/conf
SQOOP_JAR=$SQOOP_HOME/sqoop-1.4.2-gphd-3.1.0.0.jar

# Set the pig path
PIG_HOME=/usr/lib/gphd/pig
PIG_JAR=$PIG_HOME/pig.jar

# Set the hadoop variables
HADOOP_HOME=/usr/lib/gphd/hadoop
HADOOP_CONF_DIR=/etc/gphd/hadoop/conf
HADOOP_CLIENT_HOME=/usr/lib/gphd/hadoop/client

# Set some variables for configuring the classpath
CORE_HADOOP=$HADOOP_CLIENT_HOME/*

# Hive jars
HIVE_HOME=/usr/lib/gphd/hive/lib
HIVE_JARS=$HIVE_HOME/hive-common.jar:$HIVE_HOME/hive-exec.jar:$HIVE_HOME/hive-jdbc.jar:$HIVE_HOME/hive-service.jar

SQOOP_DRIVERS=/usr/lib/gphd/sqoop/lib/db2jcc4.jar:/usr/lib/gphd/sqoop/lib/db2jcc.jar:/usr/lib/gphd/sqoop/lib/ojdbc6.jar:/usr/lib/gphd/sqoop/lib/sqljdbc4.jar:/usr/lib/gphd/sqoop/lib/hsqldb-1.8.0.10.jar

SPARK_HOME=/hadoop/lib/spark-1.0.2-bin-hadoop2
SPARK_JAR=$SPARK_HOME/lib/spark-assembly-1.0.2-hadoop2.2.0.jar

# Configuration for Java / Application
APPLICATION_JAR=$PROJECT_DIR/artifacts/dataflow-engine-0.3.0.jar
APPLICATION_CONF_DIR=$PROJECT_DIR/conf
APPLICATION_LIB_DIR=$PROJECT_DIR/lib/*

# Set the entry point
ENTRY_POINT=com.samsungaustin.yac.workflow.EntryPoint

# Group some of the path stuff together
CONFIG_DIRS=$SQOOP_CONF_DIR:$HADOOP_CONF_DIR:$APPLICATION_CONF_DIR
JAR_DIRS=$CORE_HADOOP:$SQOOP_DRIVERS:$SQOOP_JAR:$HIVE_JARS:$PIG_JAR:$SPARK_JAR:$APPLICATION_JAR
LIB_DIRS=$APPLICATION_LIB_DIR

# Launch the application
# echo Executing: java -cp "$CONFIG_DIRS:$LIB_DIRS:$JAR_DIRS" -Dapp.currentDirectory=$PROJECT_DIR $ENTRY_POINT $@
                  java -cp "$CONFIG_DIRS:$LIB_DIRS:$JAR_DIRS" -Dapp.currentDirectory=$PROJECT_DIR $ENTRY_POINT $@