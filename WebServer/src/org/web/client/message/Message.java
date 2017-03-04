package org.web.client.message;

import org.web.client.Client;

public abstract class Message {

	protected Client client;
	protected String message;
	
	/**
	 * Function Constructor Message
	 * @param Client c
	 */
	public Message(Client c)
	{
		this.client = c;
	}
	
	/**
	 * @return Client
	 */
	public Client getClient()
	{
		return (this.client);
	}
	
	/**
	 * @return String message
	 */
	public String getMessage()
	{
		return (this.message);
	}
	
	/**
	 * Function for Formated message
	 * @return Succes
	 */
	public abstract boolean serialize();
}
