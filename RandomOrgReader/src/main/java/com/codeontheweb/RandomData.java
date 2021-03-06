package com.codeontheweb;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.gson.Gson;

@Entity
public class RandomData
{
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;

	private String randomData;
	private String encoding;//Description of the encoding, example "Base64", "IntegerBase10"
	private LocalDateTime creationDate;
	private Long batchId;
	
	private static final String HEX_PREFIX = "0x";
	
	//Default encoding types
	public static final String BASE64 = "Base64";
	public static final String INTEGER_BASE_10 = "IntegerBase10";
	public static final String HEX_INTEGER = "HexadecimalInteger";

	public RandomData( String randomData, String encoding, Long batchId )
	{
		this.setEncoding( encoding );
		if( HEX_INTEGER.equals(encoding) && randomData != null && !randomData.isEmpty() && !randomData.startsWith(HEX_PREFIX) )
		{
			this.setRandomData( HEX_PREFIX + randomData );
		}
		else
		{
			this.setRandomData( randomData );
		}
		this.setBatchId( batchId );
		this.setCreationDate( LocalDateTime.now() );
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

	public LocalDateTime getCreationDate()
	{
		return creationDate;
	}

	public void setCreationDate(LocalDateTime newCreationDate)
	{
		creationDate = newCreationDate;
	}

	public Long getBatchId()
	{
		return batchId;
	}

	public void setBatchId(Long newBatchId)
	{
		batchId = newBatchId;
	}
	
	public String toString()
	{
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
