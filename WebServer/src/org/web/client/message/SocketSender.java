package org.web.client.message;

import org.web.Console;
import org.web.Console.Color;
import org.web.client.Client;

public class SocketSender {

	public static void sendMessage(Message message)
	{
		if (message.getClient() == null)
			return ;
		if (message.serialize() == false)
			return ;
		Console.println("[SEND Message] " + message.getMessage(), Color.BLUE);
		try
		{
			byte[] response = message.getClient().aks.getcodec()
					.encode(message.getMessage().getBytes("UTF-8"), 0, true);
			message.getClient().aks.getoutStream()
				.write(response, 0, response.length);
			message.getClient().aks.getoutStream().flush();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void sendMessage(Client client, String message)
	{
		if (client == null)
			return ;
		if (message == null)
			return ;
		Console.println("[SEND Message] " + message, Color.BLUE);
		try
		{
			byte[] response = client.aks.getcodec()
					.encode(message.getBytes("UTF-8"), 0, true);
			client.aks.getoutStream()
				.write(response, 0, response.length);
			client.aks.getoutStream().flush();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
