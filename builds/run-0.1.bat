@ECHO OFF

set HOST=127.0.0.1
set USERNAME=username
set PASSWORD=password
set SERVER_LOG_PATH=/server/logs/jboss/application.log
set SERVER_RESULT_LOG_PATH=/tmp/sbh/application.log
set LOCAL_DIR_PATH=D:/logs

java -jar -Dhost=%HOST% -Dusername=%USERNAME% -Dpassword=%PASSWORD% -DserverLogPath=%SERVER_LOG_PATH% -DserverResultLogPath=%SERVER_RESULT_LOG_PATH% -DlocalDirPath=%LOCAL_DIR_PATH% logloader-0.1.jar