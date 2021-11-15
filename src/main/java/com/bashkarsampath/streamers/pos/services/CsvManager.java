package com.bashkarsampath.streamers.pos.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.univocity.parsers.common.processor.RowListProcessor;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

//@Slf4j
public class CsvManager {
	private CsvManager() {
	}

//	public static List<Map<?, ?>> parseCsv(InputStream inputStream) throws IOException {
////		return new CsvMapper().reader().forType(Map.class).with(CsvSchema.emptySchema().withHeader())
////				.readValues(inputStream).readAll().parallelStream().filter(Map.class::isInstance).map(Map.class::cast)
////				.collect(Collectors.toCollection());
//
//		CsvParserSettings settings = new CsvParserSettings(); // you'll find many options here, check the tutorial.
//		RowProcessor rowProcessor = new
//		settings.setProcessor(new ConcurrentRowProcessor(rowProcessor));
//		CsvParser parser = new CsvParser(settings);
//		parser.parse(csvFile);
//		// parses all rows in one go (you should probably use a RowProcessor or iterate row by row if there are many rows)
//		List<String[]> allRows = parser.parseAll(new File("/path/to/your.csv"));

	public static List<Record> parseCsv(InputStream inputStream) throws IOException {
		CsvParserSettings settings = new CsvParserSettings();
		settings.setProcessor(new RowListProcessor());
		CsvParser parser = new CsvParser(settings);
		return parser.parseAllRecords(inputStream);
	}
}
