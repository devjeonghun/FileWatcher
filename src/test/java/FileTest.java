import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class FileTest {

	@DisplayName("파일 복사 테스트")
	@Test
	public void fileCopyTest() throws InterruptedException {
		
		Path filePath = Paths.get("C:/Temp/random_text_" + new Date().getTime() + ".txt");
		long fileSize = 1L * 1024 * 1024 * 1024; // 1 gigabyte

		try {
			Files.createFile(filePath);

			RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "rw");
			FileChannel channel = file.getChannel();

			FileLock lock = channel.lock();

			byte[] data = new byte[1024]; // 1KB buffer
			while (channel.size() < fileSize) {
				channel.write(ByteBuffer.wrap(data));
			}
			System.out.println("File created and locked successfully.");
			lock.release();
			channel.close();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//assertThat().isEqualTo(6);
	}
}
