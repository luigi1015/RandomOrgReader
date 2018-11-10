package com.codeontheweb;

import java.util.HashMap;
import java.util.Map;

public class RequestJson
{
	private String jsonrpc;
	private String method;
	private Map<String, Object> params = new HashMap<String, Object>();
	private long id;

	public String getJsonrpc()
	{
		return jsonrpc;
	}

	public void setJsonrpc(String newJsonrpc)
	{
		jsonrpc = newJsonrpc;
	}

	public String getMethod()
	{
		return method;
	}

	public void setMethod(String newMethod)
	{
		method = newMethod;
	}
	
	public Map<String, Object> getParams()
	{
		return params;
	}
	
	public void addToParams( String key, Object value )
	{
		params.put(key, value);
	}
	
	public void clearParams()
	{
		params.clear();
	}

	public long getId()
	{
		return id;
	}

	public void setId(long newId)
	{
		id = newId;
	}
}
