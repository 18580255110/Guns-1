@echo off

set CURR_DIR=%cd%
set BUILD_NAME=api
set BUILD_VERSION=1.0-RELEASE

REM mvn install parent
cd ..
call mvn clean source:jar install -N -Dmaven.test.skip=true
cd %CURR_DIR%

REM mvn install core
cd ../guns-core

call mvn clean source:jar install -Dmaven.test.skip=true
cd %CURR_DIR%

REM package admin
call mvn clean package -Dmaven.test.skip=true

:Done

echo 生产环境打包完毕

pause