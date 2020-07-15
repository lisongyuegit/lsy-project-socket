@ECHO OFF
set CLASSPATH=%JAVA_HOME%/lib
set JAVA=%JAVA_HOME%/bin/java
cd %~dp0
set CLASSPATH=%CLASSPATH%;./conf
set JAVA_OPTIONS=-Djava.ext.dirs="./lib"

"%JAVA%" -Xms512m -Xmx1024m -classpath "%CLASSPATH%" %JAVA_OPTIONS% CollectServiceApplication
