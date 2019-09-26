#!/bin/sh

CURR_DIR=pwd
BUILD_NAME=admin
BUILD_VERSION=1.0-RELEASE

##mvn install parent
cd ..
mvn clean source:jar install -Dmaven.test.skip=true
#cd $CURR_DIR

#mvn install core
cd ../guns-core
mvn clean source:jar install -Dmaven.test.skip=true
cd $CURR_DIR

#package admin
mvn clean package -Dmaven.test.skip=true

echo 生产环境打包完毕


