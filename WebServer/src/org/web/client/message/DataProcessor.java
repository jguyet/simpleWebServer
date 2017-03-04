package org.web.client.message;

import org.web.Console;
import org.web.Console.Color;
import org.web.client.Client;

public class DataProcessor {

	public void postProcess(char type, char action, String message, Client client)
	{
		Console.println("[NEW MESSAGE] type[" + type + "] action [" + action + "] message [" + message + "]", Color.EXCHANGE);
	}
}
