package com.khasky.kfsw;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author Khasky
 */
public class ProcessReader extends Thread
{
	private BufferedReader reader = null;

	public ProcessReader(BufferedReader reader)
	{
		this.reader = reader;
	}

	@Override
	public void run()
	{
		String line = null;

		try
		{
			while ((line = reader.readLine()) != null)
			{
				OutputHandler.getInstance().parseLine(line);

				if (Config.UCC_RECENT_LOG_ENABLED)
				{
					LogStore.getInstance().saveLine(line);
				}
			}
		}
		catch (IOException e)
		{
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
