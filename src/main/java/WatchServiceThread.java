import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

public class WatchServiceThread extends Thread {
	
	private static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static String SOURCE_DIR = "C:/Temp";
	private static String TARGET_DIR = "C:/Copy";
	
	@Override
	public void run() {
		try {
			WatchService watchService = FileSystems.getDefault().newWatchService();
			Path directory = Paths.get(SOURCE_DIR);
			directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

			while (true) {
				WatchKey watchKey = watchService.take();
				List<WatchEvent<?>> list = watchKey.pollEvents();
				for (WatchEvent watchEvent : list) {
					WatchEvent.Kind kind = watchEvent.kind();
					Path path = (Path) watchEvent.context();
					if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
						LOGGER.info("파일 생성 -> {}", path.getFileName());
						Path sourceFile = (Path) watchEvent.context();
						Path changedFile = directory.resolve(sourceFile);
						Path targetFile = Paths.get(TARGET_DIR, sourceFile.getFileName().toString());
						checkFileChangeCompletion(changedFile);
						fileCopy(changedFile, targetFile);
					}
				}
				boolean valid = watchKey.reset();
				if (!valid) {
					break;
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}

	private void checkFileChangeCompletion(Path sourceFile) {
		try {
			long previousSize = -1;
			long currentSize = Files.size(sourceFile);
			while (currentSize != previousSize) {
				Thread.sleep(1000);
				previousSize = currentSize;
				currentSize = Files.size(sourceFile);
				LOGGER.info("파일 쓰기 중 -> {}[{}kb]", sourceFile.getFileName(), currentSize/1024);
			}
			LOGGER.info("파일 쓰기 완료 -> {}[{}kb]", sourceFile.getFileName(), currentSize/1024);
		} catch (IOException | InterruptedException e) {
			LOGGER.error(e.getMessage());
		}
	}

	private void fileCopy(Path sourceFile, Path targetFile) {
		try {
			Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
			LOGGER.info("파일 복사 완료 -> {}", targetFile.toAbsolutePath());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
	}
}
