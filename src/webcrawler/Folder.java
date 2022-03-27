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
	
	public boolean updateFiles(Crawler crawler, Path rootPath) {
		final Path currentPath = Path.of(rootPath.toString(), name);
		if (!Files.isDirectory(currentPath)) {
			try {
				Files.createDirectory(currentPath);
				System.out.println("Created Directory: " + currentPath);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		// need to be careful with the reduce that every call is made even if something is found.
		boolean foundAnything = subfolders.parallelStream()
				.map(folder -> folder.updateFiles(crawler, currentPath))
				.reduce(Boolean.FALSE, Boolean::logicalOr);
		return files.parallelStream()
					   .map(file -> file.updateFile(crawler, currentPath))
					   .reduce(Boolean.FALSE, Boolean::logicalOr) || foundAnything;
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
