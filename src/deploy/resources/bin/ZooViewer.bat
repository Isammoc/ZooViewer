@echo off

setlocal

set TITLE=ZooViewer - %ZOO_HOST%
title %TITLE%

java -cp "..\lib\*" net.isammoc.zooviewer.App %*

endlocal
