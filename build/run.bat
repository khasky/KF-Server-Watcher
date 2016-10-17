@echo off
set servername=Killing Floor Dedicated
set config=kfsw.ini
cls
title %servername% - KFServerWatcher
java -jar KFServerWatcher-v0.3.8.jar %config%
pause