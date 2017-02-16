package org.web.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;

import javax.xml.bind.DatatypeConverter;

import org.web.Console;
import org.web.Console.Color;
import org.web.Start;
import org.web.utils.WebSocketCryptage;

public class Client implements Runnable{
	
	public  Socket								session;
	public  BufferedReader 						_in;
	public	InputStream							_inStream;
	public  Thread 								_t;
	public  PrintWriter 						_out;
	public	OutputStream						_outStream;
	public WebSocketCryptage					_codec;
	
	private int									id;
	private String								key;

	public Client(int id, Socket session)
	{
		try
		{
			System.out.println("NEW CLIENT " + id);
			this.id = id;
			this.session = session;
			_inStream = this.session.getInputStream();
			_in = new BufferedReader(new InputStreamReader(this.session.getInputStream()));
			_outStream = this.session.getOutputStream();
			_out = new PrintWriter(this.session.getOutputStream());
			_codec = new WebSocketCryptage(true, false);
			_t = new Thread(this);
			_t.setDaemon(true);
			_t.setPriority(Thread.NORM_PRIORITY);
			_t.start();
		}
		catch(IOException e)
		{
			kick();
		}
	}
	
	public void sendConnexion()
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
					key = str.split("Sec-WebSocket-Key:")[1].trim();
				}
	        }
			byte[] response = (
					"HTTP/1.1 101 OK\r\n"
			        + "Connection: Upgrade\r\n"
			        + "Upgrade: websocket\r\n"
			        + "Sec-WebSocket-Accept: "
			        + DatatypeConverter.printBase64Binary(MessageDigest.getInstance("SHA-1")
			                .digest((key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
			        + "\r\n\r\n")
			        .getBytes("UTF-8");
			_outStream.write(response, 0, response.length);
			_outStream.flush();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try
    	{
	        sendConnexion();
			String message = "";
			
	    	while(Start.isRunning)
	    	{
	    		byte[] byteMessage = _codec.decode(_inStream);
	    		
	    		message = new String(byteMessage);
	    		
	    		if(!message.isEmpty())
		    	{
	    			message = toUnicode(message);
	    			postProcess(message);
		    		message = "";
		    	}
	    	}
    	}
		catch(Exception e)
    	{
			
    	}
    	finally
    	{
    		this.kick();
    	}
	}
	
	public void sendMessage(String message)
	{
		Console.println("[SEND Message] " + message, Color.BLUE);
		try
		{
			byte[] response = _codec.encode(message.getBytes("UTF-8"), 0, true);
			
			_outStream.write(response, 0, response.length);
			_outStream.flush();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void postProcess(String message)
	{
		Console.println("[NEW Message] " + message, Color.BLUE);
	}
	
	public int getId()
	{
		return (this.id);
	}
	
	public void kick()
	{
		try
		{
			Start.webServer.removeClient(this);
			if(!session.isClosed())
			{
				this.sendMessage("GK");
	    		session.close();
			}
	    	_in.close();
	    	_out.close();
	    	Console.println("Client disconnected ", Color.BLACK);
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
	}
	
	public static String toUnicode(String _in)
	{
		String _out = "";

		try
		{
			_out = new String(_in.getBytes(),"UTF8");
			
		}catch(Exception e)
		{
			System.out.println ("Conversion en UTF-8 echoue! : "+e.getMessage());
		}
		
		return _out;
	}
}
