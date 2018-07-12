package com.khasky;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.Calendar;

/**
 * @author Khasky
 */
public class FileLogger
{
	private static FileLogger _instance;
	
	public static FileLogger getInstance()
	{
		if (_instance == null) {
			_instance = new FileLogger();
		}
		return _instance;
	}
	
	public FileLogger(){}
	
	public static boolean write(String path, int maxFileSize, String line)
	{
		File file = new File(path);
		
		if (!file.exists())
		{
			try {
				file.createNewFile();
			}
			catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		else if (file.length() / 1024 > maxFileSize)
		{
			String basicName = file.getName();
			
			String name = basicName;
			String ext = "";
			
			int pos = basicName.lastIndexOf(".");
			
			if (pos > 0) {
				name = basicName.substring(0, pos);
				ext = basicName.substring(pos, basicName.length());
			}
			
			String suffix = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
			
			if (!ext.isEmpty()) {
				basicName = name + "_" + suffix + ext;
			}
			else {
				basicName += "_" + suffix;
			}
			
			File file2 = new File(basicName);
			
			file.renameTo(file2);
		}
		
		try {
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write(new SimpleDateFormat(Config.FILE_OUTPUT_DATE_FORMAT).format(Calendar.getInstance().getTime()) + " " + line + "\n");
			
			bw.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
}