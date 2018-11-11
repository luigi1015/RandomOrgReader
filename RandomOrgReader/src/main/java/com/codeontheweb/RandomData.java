package com.codeontheweb;

public class RandomData
{
	private String randomData;
	private String encoding;//Description of the encoding, example "Base64", "Integer"
	
	//Default encoding types
	public static final String BASE64 = "Base64";
	public static final String INTEGER_BASE_10 = "IntegerBase10";

	public RandomData( String randomData, String encoding )
	{
		this.randomData = randomData;
		this.setEncoding(encoding);
	}
	
	public String getRandomData()
	{
		return randomData;
	}
	
	public void setRandomData( String newRandomData )
	{
		randomData = newRandomData;
	}

	public String getEncoding()
	{
		return encoding;
	}

	public void setEncoding(String newEncoding)
	{
		encoding = newEncoding;
	}
}
