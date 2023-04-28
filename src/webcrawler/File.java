package webcrawler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class File {
	
	private final String name;
	private final Path file;
	private final FileTime lastModified;
	
	public File(Path file) {
		this.name = file.getFileName().toString();
		try {
			this.lastModified = Files.getLastModifiedTime(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.file = file;
	}
	
	public boolean updateFile(Path rootPath) {
		final Path currentPath = Path.of(rootPath.toString(), name);
		try {
			if (!Files.isRegularFile(currentPath)) {
				System.out.println("Creating file: " + currentPath);
				Files.copy(file, currentPath, StandardCopyOption.REPLACE_EXISTING);
				Files.setLastModifiedTime(currentPath, Files.getLastModifiedTime(file));
				return true;
			} else {
				final int difference = Files.readAttributes(currentPath,
						BasicFileAttributes.class).lastModifiedTime().compareTo(lastModified);
				if (difference != 0) {
					System.out.println("Updating file: " + currentPath);
					Files.copy(file, currentPath, StandardCopyOption.REPLACE_EXISTING);
					Files.setLastModifiedTime(currentPath, Files.getLastModifiedTime(file));
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public String toString() {
		return name + "\t\t\t" + lastModified;
	}
}
