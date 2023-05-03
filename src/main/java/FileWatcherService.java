public class FileWatcherService {
	public static void main(String[] args) {
		Thread watchServiceThread = new WatchServiceThread();
		watchServiceThread.start();
	}
}