package webcrawler;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class File {
	
	private final String name;
	private final FileTime lastModified;
	private final URL link;
	
	public File(String name, FileTime lastModified, URL link) {
		this.name = name;
		this.lastModified = lastModified;
		this.link = link;
	}
	
	public void updateFile(Crawler crawler, Path rootPath) {
		final Path currentPath = Path.of(rootPath.toString(), name);
		try {
			if (!Files.isRegularFile(currentPath)) {
				System.out.println("Creating file: " + currentPath);
				crawler.downloadFile(currentPath, link);
			} else {
				final int difference = Files.readAttributes(currentPath,
						BasicFileAttributes.class).lastModifiedTime().compareTo(lastModified);
				if (difference != 0) {
					System.out.println("Updating file: " + currentPath);
					crawler.downloadFile(currentPath, link);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public String toString() {
		return name + "\t\t\t" + lastModified;
	}
}
