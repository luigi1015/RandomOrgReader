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
import java.util.concurrent.TimeUnit;

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

	@Autowired
	private RandomDataRepository randomDataRepository;
	
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
		return items -> {
			for( RandomData rData : items )
			{
				logger.info("Saving data {}", rData);
				randomDataRepository.save( rData );
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
	
	private List<RandomData> getRandomData( Long bitsLeft, Long requestsLeft )
	{
		Long blobSize = 32L;//Size of a blob (in bits) to get from Random.org
		Long intSize = 10L;//Size of an integer (in bits) to get from Random.org
		Long advisoryDelay = 0L;//Per Random.org, the number of milliseconds to delay before issuing another request to Random.org
		Long divisor = 1L;//Number to divide the amount of results by. Change to something high, like 1000, when testing.
		List<RandomData> randomDataList = new ArrayList<>();

		//Get the Base64 blobs
		if( requestsLeft > 0 && bitsLeft >= blobSize )
		{
			RequestJson requestJson = new RequestJson();

			Long numBlobsLeft = bitsLeft/2/blobSize/divisor;
			while( numBlobsLeft > 0 && requestsLeft > 0)
			{
				//Random.org has a max number of blobs it'll return in one request.
				//Make sure no request is for more than the max.
				Long maxBlobs = 100L;
				Long numBlobs = numBlobsLeft <= maxBlobs ? numBlobsLeft : maxBlobs;
				numBlobsLeft -= numBlobs;

				//Set up the requestJson object for getting blobs
				requestJson.setId( batchId );
				requestJson.setJsonrpc("2.0");
				requestJson.setMethod("generateBlobs");
				requestJson.addToParams("apiKey", apiKey);
				requestJson.addToParams("n", numBlobs);
				requestJson.addToParams("size", blobSize);
				requestJson.addToParams("format", "base64");
				
				Gson gson = new Gson();
				String requestBodyString = gson.toJson(requestJson);

				try
				{
					String reponse = getPostResponse( requestBodyString );
					logger.info("Response: {}", reponse );
					JsonParser jsonParser = new JsonParser();
					JsonObject jsonResponseObject = jsonParser.parse(reponse).getAsJsonObject();
					JsonObject jsonResultObject = jsonResponseObject.getAsJsonObject("result");
					JsonObject jsonRandomObject = jsonResultObject.getAsJsonObject("random");
					JsonArray jsonRandomDataArray = jsonRandomObject.getAsJsonArray("data");
					jsonRandomDataArray.forEach( item -> {
						JsonPrimitive jsonRandomDataPrimitive = (JsonPrimitive) item;
						randomDataList.add( new RandomData(jsonRandomDataPrimitive.getAsString(), RandomData.BASE64, batchId) );
					});
		
					//Get the bits and requests left for the integers request
					bitsLeft = jsonResultObject.get("bitsLeft").getAsLong();
					requestsLeft = jsonResultObject.get("requestsLeft").getAsLong();
					advisoryDelay = jsonResultObject.get("advisoryDelay").getAsLong();
					logger.info( "Bits left: {}", bitsLeft );
					logger.info( "Requests left: {}", requestsLeft );
	
					//Wait number of milliseconds advised by Random.org
					waitMills( advisoryDelay );
				}
				catch( IOException ioe )
				{
					logger.error("Error getting the base64 blobs.", ioe);
				}
			}
		}

		//Get the integers
		if( requestsLeft > 0 && bitsLeft >= intSize )
		{
			RequestJson requestJson = new RequestJson();

			Long numIntsLeft = bitsLeft/intSize/divisor;
			while( numIntsLeft > 0 && requestsLeft > 0 )
			{
				//Random.org has a max number of integers it'll return in one request.
				//Make sure no request is for more than the max.
				Long maxInts = 1000L;
				Long numInts = numIntsLeft <= maxInts ? numIntsLeft : maxInts;
				numIntsLeft -= numInts;

				//Set up the requestJson object for getting blobs
				requestJson.setId( batchId );
				requestJson.setJsonrpc("2.0");
				requestJson.setMethod("generateIntegers");
				requestJson.addToParams("apiKey", apiKey);
				requestJson.addToParams("n", numInts);//The number of integers to get
				requestJson.addToParams("min", 0);//Minimum integer in the results (inclusive)
				requestJson.addToParams("max", 1000);//Maximum integer in the results (inclusive)
				requestJson.addToParams("replacement", true);//Replacement = true means allow duplicate values in the results
				requestJson.addToParams("base", 10);//Numerical base for the numbers (10 is the default, but 2, 8, and 16 are also allowed)
				
				Gson gson = new Gson();
				String requestBodyString = gson.toJson(requestJson);

				try
				{
					String reponse = getPostResponse( requestBodyString );
					logger.info("Response: {}", reponse );
					JsonParser jsonParser = new JsonParser();
					JsonObject jsonResponseObject = jsonParser.parse(reponse).getAsJsonObject();
					JsonObject jsonResultObject = jsonResponseObject.getAsJsonObject("result");
					JsonObject jsonRandomObject = jsonResultObject.getAsJsonObject("random");
					JsonArray jsonRandomDataArray = jsonRandomObject.getAsJsonArray("data");
					jsonRandomDataArray.forEach( item -> {
						JsonPrimitive jsonRandomDataPrimitive = (JsonPrimitive) item;
						randomDataList.add( new RandomData(jsonRandomDataPrimitive.getAsString(), RandomData.INTEGER_BASE_10, batchId) );
					});
		
					//Get the bits and requests left for the integers request
					bitsLeft = jsonResultObject.get("bitsLeft").getAsLong();
					requestsLeft = jsonResultObject.get("requestsLeft").getAsLong();
					advisoryDelay = jsonResultObject.get("advisoryDelay").getAsLong();
					logger.info( "Bits left: {}", bitsLeft );
					logger.info( "Requests left: {}", requestsLeft );
	
					//Wait number of milliseconds advised by Random.org
					waitMills( advisoryDelay );
				}
				catch( IOException ioe )
				{
					logger.error("Error getting the integers.", ioe);
				}
			}
		}

		return randomDataList;
	}

	private void waitMills( Long mills )
	{
		try
		{
			logger.info("Waiting {} milliseconds as advised by Random.org.", mills);
			TimeUnit.MILLISECONDS.sleep(mills);
		} catch( InterruptedException ie )
		{
			logger.error("Error sleeping for the requested number of milliseconds from Random.org.", ie);
		}
	}
	
	private String getPostResponse( String requestBody ) throws IOException
	{
		URL urlObj = new URL( apiURL );
		HttpURLConnection httpCon = (HttpURLConnection) urlObj.openConnection();
		httpCon.setRequestMethod( "POST" );
		httpCon.setRequestProperty( "User-Agent", "RandomOrgReaderBatch" );
		httpCon.setRequestProperty( "Content-Type", "application/json" );
		httpCon.setDoOutput(true);

		logger.info("Sending request json {}", requestBody);
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
