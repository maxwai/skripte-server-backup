package webcrawler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class Folder {
	
	private final List<Folder> subfolders;
	private final List<File> files;
	private final String name;
	
	public Folder(List<Folder> subfolders, List<File> files, String name) {
		this.subfolders = subfolders;
		this.files = files;
		this.name = name;
	}
	
	public void updateFiles(Crawler crawler, Path rootPath) {
		final Path currentPath = Path.of(rootPath.toString(), name);
		if (!Files.isDirectory(currentPath)) {
			try {
				Files.createDirectory(currentPath);
				System.out.println("Created Directory: " + currentPath);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		subfolders.forEach(folder -> folder.updateFiles(crawler, currentPath));
		files.forEach(file -> file.updateFile(crawler, currentPath));
	}
	
	@Override
	public String toString() {
		return name + "\n\t" +
			   subfolders.stream()
					   .map(folder -> "\t" + folder.toString()
							   .replaceAll("\n", "\n\t"))
					   .collect(Collectors.joining("\n\t")) +
			   (subfolders.isEmpty() ? "" : "\n") +
			   files.stream().map(files -> "\t" + files.toString())
					   .collect(Collectors.joining("\n\t")) +
			   (files.isEmpty() ? "" : "\n");
	}
}
