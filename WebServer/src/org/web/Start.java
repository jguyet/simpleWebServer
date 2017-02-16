package org.web;


import org.web.server.WebServer;

import ch.qos.logback.classic.Level;

public class Start {

	public static int				port = 9998;
	public static boolean			isRunning = true;
	public static WebServer			webServer;
	
	public static void main(String ...args)
	{	
		//Charge la console
		Console.initialize();
		Console.refreshTitle();
		Console.begin();
		
		//Charge le serveur Web
		webServer = new WebServer();
		webServer.initialize();
	}
}
