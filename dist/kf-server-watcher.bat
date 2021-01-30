rem # Killing Floor Server Watcher
rem # Copyright © Khasky (https://github.com/khasky/killing-floor-server-watcher)
@echo off
set servername=Killing Floor Dedicated
set config=kf-server-watcher.properties
cls
title %servername% - KFServerWatcher
java -jar kf-server-watcher-{{version}}.jar %config%
pause
