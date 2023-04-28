package webcrawler;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Crawler {
	
	private final URL website;
	private final java.io.File directory;
	
	public Crawler(String website, java.io.File directory) throws MalformedURLException {
		this.website = new URL(website);
		this.directory = directory;
	}
	
	// protect zip slip attack
	public static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir) throws IOException {
		// test zip slip vulnerability
		// Path targetDirResolved = targetDir.resolve("../../" + zipEntry.getName());
		Path targetDirResolved = targetDir.resolve(zipEntry.getName());
		
		// make sure normalized file still has targetDir as its prefix else throw exception
		Path normalizePath = targetDirResolved.normalize();
		if (!normalizePath.startsWith(targetDir)) {
			throw new IOException("Bad zip entry: " + zipEntry.getName());
		}
		return normalizePath;
	}
	
	public Folder download() throws IOException {
		java.io.File zipPath = new java.io.File(directory, "download.zip");
		HttpURLConnection connection = (HttpURLConnection) website.openConnection();
		connection.setRequestMethod("GET");
		try (InputStream in = connection.getInputStream()) {
			Files.copy(in, zipPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		try (ZipFile zipFile = new ZipFile(zipPath)) {
			zipFile.stream()
					.parallel()
					.forEach(entry -> {
						try {
							Path newPath = zipSlipProtect(entry, directory.toPath());
							if (!entry.isDirectory()) {
								if (newPath.getParent() != null) {
									if (Files.notExists(newPath.getParent())) {
										Files.createDirectories(newPath.getParent());
									}
								}
								Files.copy(zipFile.getInputStream(entry), newPath,
										StandardCopyOption.REPLACE_EXISTING);
							} else {
								Files.createDirectories(newPath);
							}
							Files.setLastModifiedTime(newPath, entry.getLastModifiedTime());
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});
		}
		//noinspection ResultOfMethodCallIgnored
		new java.io.File(directory, "download.zip").delete();
		return getContent(directory.toPath().resolve("Skripte"));
	}
	
	private Folder getContent(Path directory) {
		try {
			List<Folder> subfolders = new LinkedList<>();
			List<File> files = new ArrayList<>();
			try (Stream<Path> stream = Files.list(directory)) {
				stream.parallel()
						.forEach(path -> {
							if (Files.isDirectory(path)) {
								subfolders.add(getContent(path));
							} else {
								files.add(new File(path));
							}
						});
			}
			return new Folder(subfolders, files, directory.getFileName().toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
