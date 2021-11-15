package com.bashkarsampath.streamers.pos;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;

import com.bashkarsampath.streamers.pos.configurations.SftpConnector;
import com.bashkarsampath.streamers.pos.configurations.ThreadPoolConfiguration;
import com.bashkarsampath.streamers.pos.services.CsvManager;
import com.univocity.parsers.common.record.Record;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = { "com.bashkarsampath.streamers.pos.*" })
@EnableConfigurationProperties(value = {})
@EntityScan(basePackages = { "com.bashkarsampath.streamers.pos.models" })
public class PosStreamerApplication implements CommandLineRunner {
	public static void main(String[] args) {
		SpringApplication.run(PosStreamerApplication.class, args);
	}

	private RestTemplate restTemplate = new RestTemplate();

	@Override
	public void run(String... args) throws Exception {
		log.info("Application Started");
		log.info("Number of processors: " + Runtime.getRuntime().availableProcessors());
		long lStartTime = new Date().getTime();
		InputStream inputStream = SftpConnector.getFileAsInputStream("pos_utf_2021_11_13.csv");
		List<Record> records = CsvManager.parseCsv(inputStream);
		log.info("Size: " + records.size());
		int partitionSize = (int) Math.ceil(records.size() / Double.valueOf(ThreadPoolConfiguration.getCorePoolSize()));
		log.info("Number of total records: " + records.size() + "; Average partition size: " + partitionSize);
		List<List<Record>> partitions = new ArrayList<>();
		for (int i = 0; i < records.size(); i += partitionSize) {
			partitions.add(records.subList(i, Math.min(i + partitionSize, records.size())));
		}
		for (int i = 0; i < partitions.size(); i++) {
			log.info("Sublist " + i + ": " + partitions.get(i).size());
//			process(i, partitions.get(i));
		}
		long lEndTime = new Date().getTime();
		log.info("Completed main process in : " + (lEndTime - lStartTime));
	}

	@Async
	private void process(int index, List<Record> list) {
		long lStartTime = new Date().getTime();
		list.parallelStream().forEach(x -> {
			log.info("Thread : " + Thread.currentThread().getName() + ", status: " + response());
		});
		long lEndTime = new Date().getTime();
		log.info("Completed Sublist " + index + " in : " + (lEndTime - lStartTime));
	}

	private String response() {
		return restTemplate.getForObject("http://localhost:8080", String.class);
	}
}