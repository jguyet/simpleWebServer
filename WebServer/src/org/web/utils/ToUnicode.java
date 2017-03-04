package org.web.utils;

public class ToUnicode {

	public static String parse(String _in)
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
