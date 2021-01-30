package com.khasky.kfsw;

import java.util.ArrayList;
import java.util.Queue;

import com.google.common.collect.EvictingQueue;

/**
 * @author Khasky
 */
public class LogStore
{
    private static LogStore _instance;

	public static LogStore getInstance()
	{
		if (_instance == null)
		{
			_instance = new LogStore();
		}

		return _instance;
    }

    private Queue<String> storeQueue;

    public LogStore()
    {
        this.storeQueue = EvictingQueue.create(Config.UCC_RECENT_LOG_LINES_COUNT);
    }

    public void saveLine(String line)
    {
        this.storeQueue.add(line);
    }

    public int getCount()
    {
        return this.storeQueue.size();
    }

    public ArrayList<String> getAll()
    {
        return new ArrayList<String>(this.storeQueue);
    }
}
