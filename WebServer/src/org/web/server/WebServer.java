package org.web.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.web.Console;
import org.web.Console.Color;
import org.web.Start;
import org.web.client.Client;

public class WebServer implements Runnable{
	
	private ArrayList<Client>	_clients = new ArrayList<Client>();
	private Thread				_t;
	private ServerSocket		SS;
	
	public void initialize()
	{
		try
		{
			SS = new ServerSocket(Start.port);
			_t = new Thread(this);
			_t.start();
			Console.print("Le serveur est ligne sur l'adresse suivante           ", Color.SUCCESS);
			Console.println("" + Start.port, Color.WHITE);
		}
		catch (IOException e)
		{
			sendError(e);
			//Main.stop();
		}
	}
	
	public int getClientNumber()
	{
		return (this._clients.size());
	}
	
	public void removeClient(Client c)
	{
		if (this._clients.contains(c))
			this._clients.remove(c);
	}
	
	@Override
	public void run() {
		
		while(Start.isRunning)//bloque sur _SS.accept()
		{
			try
			{
				
				Socket client = SS.accept();
				String addr = "";
				Console.debug("Reception d'une nouvelle connexion tcp");
				if (client.getInetAddress() != null)
					addr = client.getInetAddress().getHostAddress();

				if (!addr.equalsIgnoreCase(""))
				{
					_clients.add(new Client(_clients.size(), client));
				}
				else
				{
					client.close();
				}
			}catch(IOException e)
			{
				sendError(e);
			}
		}
	}
	
	private void sendError(Exception e)
	{
		e.printStackTrace();
		Console.println("Erreur GameServer : " + e.getMessage(), Console.Color.ERROR);
	}
}
