package org.web;

import org.fusesource.jansi.AnsiConsole;

public class Console
{

	public static void initialize()
	{
		Console.setTitle("WebServer - Loading...");
	}

	public static void begin()
	{
		//Console.clear();
		Console.initialize();
		Console.println("                                                                           ", Color.WHITE);
		Console.println("Le WebServeur est OK ! En attente de connexion...", Color.SUCCESS);
		Console.println("---------------------------------------------------------------------------", Color.WHITE);
	}

	public static void refreshTitle()
	{
		if (!Start.isRunning)
			return;
		if (Start.webServer == null)
			return ;
		String title = "WebServer - Port : " + Start.port + " | "
				+ " | " + Start.webServer.getClientNumber() + " Client(s)";
		Console.setTitle(title);
	}

	public static void println(Object o, Color color)
	{ //~30ms 
		AnsiConsole.out.println("\033[1m\033[" + color.get() + "m" + o
				+ "\033[0m");
	}
	
	public static void printloaded(int nbr, Color color)
	{ //~30ms 
		print("[", Color.WHITE);
		AnsiConsole.out.printf("\033[1m\033[" + color.get() + "m %5d\033[0m", nbr);
		println("]", Color.WHITE);
	}
	
	public static void debug(Object o)
	{
		AnsiConsole.out.println("\033[1m\033[" + Color.RED.get() + "m[DEBUG] " + o
				+ "\033[0m");
	}

	public static void print(Object o, Color color)
	{ //~30ms
		AnsiConsole.out.print("\033[1m\033[" + color.get() + "m" + o
				+ "\033[0m");
	}

	public static void clear()
	{ //~30ms
		AnsiConsole.out.print("\033[H\033[2J");
	}

	public static void setTitle(String title)
	{ //~50ms
		AnsiConsole.out.printf("%c]0;%s%c", '\033', title, '\007');
	}

	public enum Color
	{
		ERROR(31), //red
		SUCCESS(32), //green
		WAITING(33), //yellow
		EXCHANGE(34), //blue
		INFORMATION(35), //purple
		GAME(36), //cyan
		BOLD(1),
		UNDERLINE(4),
		BLACK(30),
		RED(31),
		GREEN(32),
		YELLOW(33),
		BLUE(34),
		WHITE(37),
		FIGHT(37);

		private int	color;

		private Color(int color)
		{
			this.color = color;
		}

		public int get()
		{
			return color;
		}
	}
}