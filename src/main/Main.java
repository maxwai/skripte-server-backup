package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
		final String savePathEnv = System.getenv("SAVE_PATH");
		final String sleepTime = System.getenv("INTERVAL");
		if (website == null || savePathEnv == null || sleepTime == null) {
			System.err.println("Need following environment variables to work: WEBSITE, SAVE_PATH");
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
			final boolean changedAnything = crawl(website, savePath);
			if (changedAnything)
				makeGitCommands(savePath.toAbsolutePath().toString());
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
	
	private static void makeGitCommands(String gitPath) {
		System.out.println("Doing git commands");
		
		final String cmd1 = "git add .";
		final String cmd2 = "git commit -m 'Changes from " + LocalDateTime.now() + "'";
		final String cmd3 = "git push";
		final List<String> cmds = List.of(cmd1, cmd2, cmd3);
		
		final Runtime runtime = Runtime.getRuntime();
		
		cmds.forEach(cmd -> {
			try {
				final Process process = runtime.exec(new String[]{"sh", "-c", cmd}, null,
						new File(gitPath));
				
				final BufferedReader reader = new BufferedReader(
						new InputStreamReader(process.getInputStream()));
				
				final BufferedReader errorreader = new BufferedReader(
						new InputStreamReader(process.getErrorStream()));
				
				final StringBuilder output = new StringBuilder();
				
				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line).append("\n");
				}
				
				output.append("\nErrors:\n\n");
				
				while ((line = errorreader.readLine()) != null) {
					output.append(line).append("\n");
				}
				
				System.out.println("Exit value: " + process.waitFor());
				System.out.println(output);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		});
		System.out.println("Finished with Git commands");
	}
	
	private static boolean crawl(String website, Path savePath) {
		try {
			final Instant start = Instant.now();
			File directory = new File("tmp");
			if (!directory.isDirectory()) {
				//noinspection ResultOfMethodCallIgnored
				directory.mkdirs();
			}
			final Crawler crawler = new Crawler(website, directory);
			final Folder folder = crawler.download();
			final boolean changedAnything = folder.updateFiles(savePath, true);
			final Instant stop = Instant.now();
			if (DEBUG)
				System.out.println("Took " + Duration.between(start, stop).toString());
			return changedAnything;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
