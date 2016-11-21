@echo off
cd ..
set FileServer_Home=%cd%
java -jar -DFileServer_Home=%FileServer_Home%  %FileServer_Home%/FileServer.jar
