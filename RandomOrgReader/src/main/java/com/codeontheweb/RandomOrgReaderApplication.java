package com.codeontheweb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

@SpringBootApplication
public class RandomOrgReaderApplication implements RequestStreamHandler
{
	public static void main(String[] args)
	{
		SpringApplication.run(RandomOrgReaderApplication.class, args);
	}

	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException
	{
		String[] args = {};
		main(args);
	}
}
