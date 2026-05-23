@echo off
echo Compiling...
javac -cp "src;lib/mysql-connector-j-8.4.0.jar" src/*.java
if %errorlevel% neq 0 exit /b %errorlevel%

echo Running GUIApp...
java -cp "src;lib/mysql-connector-j-8.4.0.jar" GUIApp
pause
