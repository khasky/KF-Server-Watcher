@echo off
set servername=Killing Floor Dedicated
set config=kfsw.properties
cls
title %servername% - KFServerWatcher
java -jar kf-server-watcher-%version%.jar %config%
pause