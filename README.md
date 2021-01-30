KF Server Watcher v0.4.2 - Killing Floor Dedicated Server Administration Tool
===================

This program was written on [Java](https://java.com) and you can use it with [Killing Floor](http://killingfloorthegame.com) game dedicated server.

The main reason behind making this tool is worthless output log of Killing Floor dedicated server console.  
We all want to see real-time information from server like player connections, current online and map change.  

P.S.  

If you need to check original logs of dedicated server console you can always look into the log file that you can set using **-log <file name>** parameter.

## Features

 * Start server with specified or random map from **[KFmod.KFMaplist]** section
 * Display players, online list and current map in real-time
 * Player details (Steam ID, IP, Name)
 * File logging (optional)
 * Database logging (optional)
 * Auto-restart on server crash
 * Tested and endorsed by server administrators

## Configuration

Single server configuration:

 * Configure settings in **kf-server-watcher.bat** for your server
 * Configure settings in **kf-server-watcher.properties** for your server

You might probably want to launch multiple servers:

 * Duplicate file **kf-server-watcher.bat** and rename to **kfsw_serv2.bat**
 * Duplicate file **kf-server-watcher.properties** and rename to **kfsw_serv2.properties**
 * Open **kfsw_serv2.bat** and change **config** variable to:  
```set config=kfsw_serv2.properties```  
 * You're done! Add more servers if you want

## Output Sample

![KFServerWatcher](https://github.com/khasky/KFServerWatcher/blob/master/screenshot.png)

## Process Exit Codes

 * 0 - Normal exit
 * 1 - Process lost
 * -1 - Process interrupted

## To Do

 * Linux support
