package com.codeontheweb;

public class RandomData
{
	//Base64 encoding of random data
	private String randomData;

	public RandomData()
	{
		//Placeholder constructor
	}

	public RandomData( String randomData )
	{
		this.randomData = randomData;
	}
	
	public String getRandomData()
	{
		return randomData;
	}
	
	public void setRandomData( String newRandomData )
	{
		randomData = newRandomData;
	}
}
