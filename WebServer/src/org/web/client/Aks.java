package org.web.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;

import javax.xml.bind.DatatypeConverter;

import org.web.Console;
import org.web.Start;
import org.web.client.message.DataProcessor;
import org.web.utils.ToUnicode;
import org.web.utils.WebSocketCryptage;

public class Aks implements Runnable{

	private BufferedReader 						_in;
	private InputStream							_inStream;
	private PrintWriter 						_out;
	private	OutputStream						_outStream;
	private WebSocketCryptage					_codec;
	private String								_key;
	private Thread 								_t;
	
	public Client								client;
	
	public static final String					WEB_SOCKET_MASK = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	
	public DataProcessor						processor = new DataProcessor();
	
	public Aks(Client client)
	{
		try {
			_inStream = client.session.getInputStream();
			_in = new BufferedReader(new InputStreamReader(client.session.getInputStream()));
			_outStream = client.session.getOutputStream();
			_out = new PrintWriter(client.session.getOutputStream());
			_codec = new WebSocketCryptage(true, false);
			_t = new Thread(this);
			_t.setDaemon(true);
			_t.setPriority(Thread.NORM_PRIORITY);
			_t.start();
		}
		catch(IOException e)
		{
			client.kick();
		}
	}
	
	@Override
	public void run() {
		try
    	{
	        if (!initConnection())
	        {
	        	client.kick();
	        	return ;
	        }
	        //Go Wait messages
	        waitMessages();
    	}
		catch(Exception e)
    	{
			Console.debug(e);
    	}
    	finally
    	{
    		client.kick();
    	}
	}
	
	public OutputStream getoutStream()
	{
		return (_outStream);
	}
	
	public InputStream getinStream()
	{
		return (_inStream);
	}
	
	public WebSocketCryptage getcodec()
	{
		return (_codec);
	}
	
	public void closeDescriptors()
	{
		try
		{
			this._in.close();
			this._out.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private boolean initConnection()
	{
		try
		{
			String str = ".";
			
	        while (!str.equals(""))
	        {
				str = _in.readLine();
				if (str == null)
					str = "";
				if (str.contains("Sec-WebSocket-Key:"))
				{
					_key = str.split("Sec-WebSocket-Key:")[1].trim();
				}
	        }
			byte[] response = (
					"HTTP/1.1 101 OK\r\n"
			        + "Connection: Upgrade\r\n"
			        + "Upgrade: websocket\r\n"
			        + "Sec-WebSocket-Accept: "
			        + DatatypeConverter.printBase64Binary(MessageDigest.getInstance("SHA-1")
			        	.digest((_key + WEB_SOCKET_MASK).getBytes("UTF-8")))
			        + "\r\n\r\n")
			        .getBytes("UTF-8");
			_outStream.write(response, 0, response.length);
			_outStream.flush();
		} catch (Exception e)
		{
			e.printStackTrace();
			return (false);
		}
		return (true);
	}
	
	private void waitMessages()
	{
		String message = "";
		
		while(Start.isRunning)
    	{
    		byte[] byteMessage = _codec.decode(_inStream);
    		
    		message = new String(byteMessage);
    		
    		if(!message.isEmpty() && !(message = ToUnicode.parse(message)).isEmpty())
	    	{
    			char type = ' ';
    			char action = ' ';
    			
    			type = message.charAt(0);
    			if (message.length() > 1)
    				action = message.charAt(1);
    			processor.postProcess(type, action, message, client);
	    		message = "";
	    	}
    	}
	}
}
