package com.codeontheweb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

@Configuration
@EnableBatchProcessing
@PropertySource({"classpath:application.properties", "classpath:private.properties"})
public class BatchConfiguration
{
	private static final Logger logger = LoggerFactory.getLogger(BatchConfiguration.class);

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Value("${RANDOM_ORG_API_URL}")
	private String apiURL;

	@Value("${RANDOM_ORG_API_KEY}")
	private String apiKey;
	
	long batchId;

	@Bean
	public ListItemReader<RandomData> randomDataReader()
	{
		List<RandomData> randomDataList = new ArrayList<>();

		try
		{
			UsageResponse usageData = getUsage();
			Long bitsLeft = Long.parseLong(usageData.getResult().get("bitsLeft"));
			Long requestsLeft = Long.parseLong(usageData.getResult().get("requestsLeft"));
			randomDataList = getRandomData( bitsLeft, requestsLeft );
		}
		catch( IOException ioe )
		{
			logger.error("Error trying to get the random data.", ioe);
		}

		return new ListItemReader<>(randomDataList);
	}

	@Bean
	public ItemWriter<RandomData> randomDataWriter()
	{
		//TODO: Write the data to a database
		return items -> {
			for( RandomData rData : items )
			{
				logger.info("Writing random data: {}", rData.getRandomData());
			}
		};
	}

	@Bean
	public Step randomDataStep()
	{
		return stepBuilderFactory.get("RandomDataStep")
				.<RandomData, RandomData>chunk(10)
				.reader(randomDataReader())
				.writer(randomDataWriter())
				.build();
	}

	@Bean
	public Job randomDataJob( Step randomDataStep )
	{
		//TODO: Maybe implement a JobCompletionNotificationListener
		//		See https://spring.io/guides/gs/batch-processing/
		return jobBuilderFactory.get("randomDataJob")
				.incrementer(new RunIdIncrementer())
				.flow(randomDataStep)
				.end()
				.build();
	}
	
	private long getDateTimeLong()
	{
		LocalDateTime nowTime = LocalDateTime.now();
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		String formattedNow = nowTime.format(timeFormatter);
		return Long.parseLong(formattedNow);
	}
	
	private UsageResponse getUsage() throws IOException
	{
		RequestJson requestJson = new RequestJson();
		
		//Set up the object for the creation of the JSON
		requestJson.setId( batchId );
		requestJson.setJsonrpc("2.0");
		requestJson.setMethod("getUsage");
		requestJson.addToParams("apiKey", apiKey);
		
		Gson gson = new Gson();
		String requestBodyString = gson.toJson(requestJson);
		
		String reponse = getPostResponse( requestBodyString );
		UsageResponse usageResponse = gson.fromJson(reponse, UsageResponse.class);
		logger.info("bitsLeft: {}", Long.parseLong(usageResponse.getResult().get("bitsLeft")) );
		logger.info("requestsLeft: {}", Long.parseLong(usageResponse.getResult().get("requestsLeft")) );
		
		return usageResponse;
	}
	
	private List<RandomData> getRandomData( Long bitsLeft, Long requestsLeft ) throws IOException
	{
		Long blobSize = 32L;
		List<RandomData> randomDataList = new ArrayList<>();
		if( requestsLeft > 0 && bitsLeft >= blobSize )
		{
			RequestJson requestJson = new RequestJson();
			
			//Set up the object for the creation of the JSON
			requestJson.setId( batchId );
			requestJson.setJsonrpc("2.0");
			requestJson.setMethod("generateBlobs");
			requestJson.addToParams("apiKey", apiKey);
			requestJson.addToParams("n", bitsLeft/blobSize/1000);//TODO: Once the code is ready for production, take out the /1000 (which is only for testing)
			requestJson.addToParams("size", blobSize);
			requestJson.addToParams("format", "base64");
			
			Gson gson = new Gson();
			String requestBodyString = gson.toJson(requestJson);
			
			String reponse = getPostResponse( requestBodyString );
			logger.info("Response: {}", reponse );
			JsonParser jsonParser = new JsonParser();
			JsonObject jsonResponseObject = jsonParser.parse(reponse).getAsJsonObject();
			JsonObject jsonResultObject = jsonResponseObject.getAsJsonObject("result");
			JsonObject jsonRandomObject = jsonResultObject.getAsJsonObject("random");
			JsonArray jsonRandomDataArray = jsonRandomObject.getAsJsonArray("data");
			jsonRandomDataArray.forEach( item -> {
				JsonPrimitive jsonRandomDataPrimitive = (JsonPrimitive) item;
				randomDataList.add( new RandomData(jsonRandomDataPrimitive.getAsString()) );
			});
		}
		return randomDataList;
	}
	
	private String getPostResponse( String requestBody ) throws IOException
	{
		URL urlObj = new URL( apiURL );
		HttpURLConnection httpCon = (HttpURLConnection) urlObj.openConnection();
		httpCon.setRequestMethod( "POST" );
		httpCon.setRequestProperty( "User-Agent", "RandomOrgReaderBatch" );
		httpCon.setRequestProperty("Content-Type", "application/json");
		httpCon.setDoOutput(true);

		OutputStream httpOS = httpCon.getOutputStream();
		httpOS.write( requestBody.getBytes() );

		int responseCode = httpCon.getResponseCode();
		logger.info( "Response code: {}", responseCode );
		
		BufferedReader responseBR = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
		String responseLine;
		StringBuffer responseSB = new StringBuffer();
		while( (responseLine = responseBR.readLine()) != null )
		{
			responseSB.append(responseLine);
		}
		responseBR.close();
		
		return responseSB.toString();
	}

	@PostConstruct
	public void initialize()
	{
		batchId = getDateTimeLong();
	}
}
