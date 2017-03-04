package org.web.client;

import java.net.Socket;

import org.web.Console;
import org.web.Console.Color;
import org.web.Start;
import org.web.client.message.SocketSender;

public class Client{
	
	public Socket								session;
	public Aks									aks;

	public Client(Socket session)
	{
		System.out.println("NEW CLIENT");
		this.session = session;
		this.aks = new Aks(this);
	}
	
	public void kick()
	{
		try
		{
			Start.webServer.removeClient(this);
			if(!session.isClosed())
			{
				SocketSender.sendMessage(this, "GK");
	    		session.close();
			}
	    	this.aks.closeDescriptors();
	    	Console.println("Client disconnected ", Color.BLACK);
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
	}
}
