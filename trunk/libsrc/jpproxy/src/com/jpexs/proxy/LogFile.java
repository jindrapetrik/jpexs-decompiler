package com.jpexs.proxy;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;

public class LogFile
{
    private static SimpleDateFormat format;
    private static String zoneString;
    private String filename = null;
    private long maxLogFileSize;
    private int maxLogFileHistory;

    static
    {
	format = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss", Locale.US); 
	format.setTimeZone(TimeZone.getDefault());
	zoneString = generateZoneString();
    }
    
    public LogFile(String filename)
    {
	setLogFile(filename);

	maxLogFileSize = ProxyConfig.maxLogFileSize;
	maxLogFileHistory = ProxyConfig.maxLogFileHistory;
    }

    public void setLogFile(String filename)
    {
	this.filename = filename;
    }

    /**
     * calculate the local timezone offset
     * for PST this is -0800
     */
    private static String generateZoneString()
    {
	Calendar calendar = Calendar.getInstance();
	int offset = calendar.get(Calendar.ZONE_OFFSET)
	    + calendar.get(Calendar.DST_OFFSET);
	StringBuffer buf = new StringBuffer();
	if (offset < 0)
	{
	    buf.append("-");
	    offset = -offset;
	}
	int hours = offset/(60 * 60 * 1000);
	int mins = (offset%(60 * 60 * 1000)) / (60 * 1000);
	if (hours < 10)
	    buf.append("0");
	buf.append(hours);
	if (mins < 10)
	    buf.append("0");
	buf.append(mins);
	return buf.toString();
    }

    private void gzip(File src)
    {
	File dst =new File(src.getAbsolutePath() + ".gz");
	FileInputStream in;
	GZIPOutputStream out;
	byte[] buf = new byte[8192];
	int n;

        try
        {
            in = new FileInputStream(src);
            out = new GZIPOutputStream(new FileOutputStream(dst), buf.length);

            while ((n = in.read(buf)) >= 0)
            {
                out.write(buf, 0, n);
            }

	    in.close();
	    out.close();
        }
	catch (IOException e)
	{
	    System.out.println(e);
	}
    }

    private void rotate(int max)
    {
	File f;
	
	for (int i = max - 2; i >= 1; i--)
	{
	    f = new File(filename + "." + i + ".gz");
	    if (f.exists())
	    {
		f.renameTo(new File(filename + "." + (i+1) + ".gz"));
	    }
	}

	f = new File(filename + ".0");
	if (f.exists())
	{
	    gzip(f);
	    f = new File(filename + ".0.gz");
	    f.renameTo(new File(filename + ".1.gz"));
	}

	f = new File(filename);
	f.renameTo(new File(filename + ".0"));
    }
    
    public synchronized void log(Request request, Reply reply)
    {
	int offset;
	Date date;

	date = new Date();
	
	StringBuffer buf = new StringBuffer();
	buf.append(request.getClient().getInetAddress().getHostAddress());
	buf.append(" - - "); // ident authuser
	buf.append("[");
	buf.append(format.format(date));
	buf.append(" ");
	buf.append(zoneString);
	buf.append("] \"");
	buf.append(request.getRequest());
	buf.append("\" ");
	buf.append(reply.getStatusCode());
	buf.append(" ");
	int length;
	try
	{
	    length = Integer.parseInt(reply.getHeaderField("Content-length"));
	}
	catch (NumberFormatException e)
	{
	    length = 0;
	}
	buf.append(length);
	buf.append("\n");

	if (!ProxyConfig.dontLogFilters)
	{
	    for (Enumeration h = request.getLogHeaders();
		 h != null && h.hasMoreElements(); )
	    {
		String header = (String)h.nextElement();
		buf.append("[");
		buf.append(header);
		buf.append("]\n");
		for (Enumeration e = request.getLogEntries(header);
		     e.hasMoreElements(); )
		{
		    buf.append("* ");
		    buf.append(e.nextElement().toString());
		    buf.append("\n");
		}
	    }
	}
	
	try
	{
	    File file = new File(filename);
	    if (file.length() > maxLogFileSize)
	    {
		rotate(maxLogFileHistory);
	    }
	    
	    FileOutputStream out = new FileOutputStream(filename, true);
	    out.write(buf.toString().getBytes());
	    out.close();
	}
	catch (IOException e)
	{
	    e.printStackTrace();
	}
    }
}
