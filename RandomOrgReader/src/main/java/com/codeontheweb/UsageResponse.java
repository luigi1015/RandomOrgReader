package com.codeontheweb;

import java.util.Map;

public class UsageResponse
{
	private String jsonrpc;
	private Long id;
	private Map<String, String> result;

	public String getJsonrpc()
	{
		return jsonrpc;
	}

	public void setJsonrpc(String newJsonrpc)
	{
		jsonrpc = newJsonrpc;
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long newId)
	{
		id = newId;
	}

	public Map<String, String> getResult()
	{
		return result;
	}

	public void setResult(Map<String, String> newResult)
	{
		result = newResult;
	}
}
