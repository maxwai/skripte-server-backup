import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import webcrawler.Crawler;
import webcrawler.Folder;

public class Main {
	
	public static void main(String[] args) {
		final String website = System.getenv("WEBSITE");
		final String username = System.getenv("USERNAME");
		final String password = System.getenv("PASSWORD");
		final String savePathEnv = System.getenv("SAVE_PATH");
		if (website == null || username == null || password == null || savePathEnv == null) {
			System.err.println(
					"Need following environment variables to work: WEBSITE, USERNAME, PASSWORD, "
					+ "SAVE_PATH");
			System.exit(-1);
		}
		
		final Path savePath = Path.of(savePathEnv);
		if (!Files.isDirectory(savePath))
			throw new IllegalArgumentException("Path is not correct");
		
		try {
			Instant start = Instant.now();
			Crawler crawler = new Crawler(website, username, password);
			List<Folder> folders = crawler.crawl();
			folders.parallelStream().forEach(folder -> folder.updateFiles(crawler, savePath));
			Instant stop = Instant.now();
			System.out.println("Took " + Duration.between(start, stop).toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
