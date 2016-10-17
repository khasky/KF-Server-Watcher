KF Server Watcher v0.3.8 - Killing Floor Dedicated Server Administration Tool
===================

This program is written on [Java](https://java.com) and you can use it along with [Killing Floor](http://killingfloorthegame.com) game dedicated server console.

The main reason for writing this tool is ugly and uninformative output of Killing Floor dedicated server console.
I want to see only useful information from server like which player is connected, how many players are online and what is the current map.

P.S.

If you need to check original logs of dedicated server console you can always look to the log file that you could set by **-log <file name>** parameter.

## Features

 * Ability to start server with random map from **[KFmod.KFMaplist]** section
 * Display players leave/join, online list, current map
 * Detailed player info (Steam ID, IP, Name)
 * File logging
 * Auto-restart on server crash
 * Already tested on [live servers](http://taey.net)

## Configuration

It's made easy!

 * Adjust settings at **run.bat** for your server
 * Adjust settings at **kfsw.ini** for your server

Also you could have a multiple servers configuration. Here is example:

 * Copy **run.bat** and rename to **run_second_server.bat**
 * Copy **kfsw.ini** and rename to **kfsw_second_server.ini**
 * Open **run_second_server.bat** and change **kfsw_config** option to:  
```set kfsw_config=kfsw_second_server.ini```  
 * Now you able to launch two servers with KF Server Watcher

## Output sample

![KFServerWatcher](https://github.com/khasky/KFServerWatcher/blob/master/screenshot.png)