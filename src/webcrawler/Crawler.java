package webcrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.net.ssl.HttpsURLConnection;

public class Crawler {
	
	private final URL website;
	private final String authentication;
	
	public Crawler(String website, String username, String password) throws MalformedURLException {
		this.website = new URL(website);
		this.authentication = Base64.getEncoder()
				.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
	}
	
	public List<Folder> crawl() throws IOException {
		//noinspection unchecked
		return (List<Folder>) this.crawlOneWebsite(website)[0];
	}
	
	private Object[] crawlOneWebsite(URL website) throws IOException {
		System.out.println("Crawling " + website);
		final HttpsURLConnection connection = this.getConnection(website);
		final List<String> lines = new ArrayList<>();
		try (InputStream content = connection.getInputStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(content))) {
			String line;
			while ((line = in.readLine()) != null) {
				lines.add(line);
			}
		}
		final List<Map<ITEMS, String>> parsedLines = lines.stream()
				.filter(line -> line.contains("<tr><td"))
				.filter(line -> !line.contains("PARENTDIR"))
				.parallel()
				.map(line -> {
					final int indexOfType = line.indexOf("alt=") + 6;
					final String type = line.substring(indexOfType, line.indexOf(']',
							indexOfType));
					final int indexOfHref = line.indexOf("<a href=") + 9;
					final int endIndexOfHref = line.indexOf('"', indexOfHref);
					String link = line.substring(indexOfHref, endIndexOfHref);
					link = link.replaceAll("&amp;", "&");
					final String name = line.substring(endIndexOfHref + 2,
							line.indexOf("</a>", endIndexOfHref));
					return Map.of(ITEMS.TYPE, type, ITEMS.LINK, link, ITEMS.NAME, name);
				})
				.toList();
		final List<File> files = parsedLines.stream()
				.filter(map -> !map.get(ITEMS.TYPE).equals("DIR"))
				.parallel()
				.map(map -> {
					try {
						URL fileURL = new URL(website, map.get(ITEMS.LINK));
						return new File(map.get(ITEMS.NAME), getLastModified(fileURL), fileURL);
					} catch (IOException e) {
						e.printStackTrace();
					}
					return null;
				})
				.filter(Objects::nonNull)
				.toList();
		final List<Folder> subfolders = parsedLines.stream()
				.filter(map -> map.get(ITEMS.TYPE).equals("DIR"))
				.parallel()
				.map(map -> {
					try {
						final Object[] output = crawlOneWebsite(
								new URL(website, map.get(ITEMS.LINK)));
						//noinspection unchecked
						return new Folder((List<Folder>) output[0], (List<File>) output[1],
								map.get(ITEMS.NAME));
					} catch (IOException e) {
						e.printStackTrace();
					}
					return null;
				})
				.filter(Objects::nonNull)
				.toList();
		return new Object[]{subfolders, files};
	}
	
	public void downloadFile(Path filePath, URL fileWebsite) throws IOException {
		System.out.println("Downloading " + fileWebsite);
		final HttpsURLConnection connection = this.getConnection(fileWebsite);
		try (InputStream input = connection.getInputStream()) {
			Files.write(filePath, input.readAllBytes());
		}
		Files.setLastModifiedTime(filePath, FileTime.fromMillis(connection.getLastModified()));
	}
	
	private HttpsURLConnection getConnection(URL website) throws IOException {
		final HttpsURLConnection connection = (HttpsURLConnection) website.openConnection();
		connection.setDoOutput(true);
		connection.setRequestProperty("Authorization", "Basic " + authentication);
		return connection;
	}
	
	private FileTime getLastModified(URL fileURL) throws IOException {
		final HttpsURLConnection connection = (HttpsURLConnection) fileURL.openConnection();
		connection.setRequestMethod("HEAD");
		connection.setRequestProperty("Authorization", "Basic " + authentication);
		return FileTime.fromMillis(connection.getLastModified());
	}
	
	private enum ITEMS {
		LINK, NAME, TYPE
	}
}
