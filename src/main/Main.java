package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import webcrawler.Crawler;
import webcrawler.Folder;

public class Main {
	
	public static boolean DEBUG = false;
	
	public static void main(String[] args) {
		final String website = System.getenv("WEBSITE");
		final String username = System.getenv("USERNAME");
		final String password = System.getenv("PASSWORD");
		final String savePathEnv = System.getenv("SAVE_PATH");
		final String sleepTime = System.getenv("INTERVAL");
		if (website == null || username == null || password == null || savePathEnv == null
			|| sleepTime == null) {
			System.err.println(
					"Need following environment variables to work: WEBSITE, USERNAME, PASSWORD, "
					+ "SAVE_PATH");
			System.exit(-1);
		}
		
		final long timeWithUnit = Long.parseLong(sleepTime.substring(0, sleepTime.length() - 1));
		long sleepTimeInMillis;
		if (sleepTime.charAt(sleepTime.length() - 1) == 'd') {
			sleepTimeInMillis = timeWithUnit * 24;
		} else if (sleepTime.charAt(sleepTime.length() - 1) == 'h') {
			sleepTimeInMillis = timeWithUnit;
		} else if (sleepTime.charAt(sleepTime.length() - 1) >= '0'
				   || sleepTime.charAt(sleepTime.length() - 1) <= '9') {
			sleepTimeInMillis = Long.parseLong(sleepTime);
		} else {
			throw new IllegalArgumentException("Interval is not correctly parsed");
		}
		
		if (sleepTimeInMillis < 1) {
			throw new IllegalArgumentException("Interval is less then 1 hour");
		}
		sleepTimeInMillis *= 60 * 60 * 1000;
		
		final String debug = System.getenv("DEBUG");
		if (debug != null)
			DEBUG = Boolean.parseBoolean(debug);
		
		final Path savePath = Path.of(savePathEnv);
		if (!Files.isDirectory(savePath))
			throw new IllegalArgumentException("Path is not correct");
		
		//noinspection InfiniteLoopStatement
		while (true) {
			crawl(website, username, password, savePath);
			try {
				System.out.println("Waiting till " +
								   LocalDateTime.now().plusSeconds(sleepTimeInMillis / 1000) +
								   " for next crawl");
				//noinspection BusyWait
				Thread.sleep(sleepTimeInMillis);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void crawl(String website, String username, String password, Path savePath) {
		try {
			Instant start = Instant.now();
			Crawler crawler = new Crawler(website, username, password);
			List<Folder> folders = crawler.crawl();
			folders.parallelStream().forEach(folder -> folder.updateFiles(crawler, savePath));
			Instant stop = Instant.now();
			if (DEBUG)
				System.out.println("Took " + Duration.between(start, stop).toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
