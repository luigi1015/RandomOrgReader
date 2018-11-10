package com.codeontheweb;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

	@Bean
	public ListItemReader<RandomData> randomDataReader()
	{
		List<RandomData> randomDataList = new ArrayList<>();

		//TODO: Populate the list with random data from Random.org
		for( int i = 0; i < 100; i++ )
		{
			randomDataList.add(new RandomData(String.valueOf(i)));
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
				logger.info(rData.getRandomData());
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
	
	private long getDateTimeInt()
	{
		LocalDateTime nowTime = LocalDateTime.now();
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		String formattedNow = nowTime.format(timeFormatter);
		return Long.parseLong(formattedNow);
	}
}
