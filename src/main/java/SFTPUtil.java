import com.jcraft.jsch.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

public class SFTPUtil {

	private Session session = null;
	private Channel channel = null;
	private ChannelSftp channelSftp = null;

	/**
	 * 서버와 연결에 필요한 값들을 가져와 초기화 시킴
	 *
	 * @param host       서버 주소
	 * @param userName   아이디
	 * @param password   패스워드
	 * @param port       포트번호
	 * @param privateKey 개인키
	 */
	public void init(String host, String userName, String password, int port, String privateKey) {

		JSch jSch = new JSch();

		try {
			if (privateKey != null) {//개인키가 존재한다면
				jSch.addIdentity(privateKey);
			}
			session = jSch.getSession(userName, host, port);

			if (privateKey == null && password != null) {//개인키가 없다면 패스워드로 접속
				session.setPassword(password);
			}

			// 프로퍼티 설정
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no"); // 접속 시 hostkeychecking 여부
			session.setConfig(config);
			session.connect();
			//sftp로 접속
			channel = session.openChannel("sftp");
			channel.connect();
		} catch (JSchException e) {
			e.printStackTrace();
		}

		channelSftp = (ChannelSftp) channel;
	}

	/**
	 * 디렉토리 생성
	 *
	 * @param dir       이동할 주소
	 * @param mkdirName 생성할 디렉토리명
	 */
	public void mkdir(String dir, String mkdirName) {
		if (!this.exists(dir + "/" + mkdirName)) {
			try {
				channelSftp.cd(dir);
				channelSftp.mkdir(mkdirName);
			} catch (SftpException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 디렉토리( or 파일) 존재 여부
	 *
	 * @param path 디렉토리 (or 파일)
	 * @return
	 */
	public boolean exists(String path) {
		Vector res = null;
		try {
			res = channelSftp.ls(path);
		} catch (SftpException e) {
			if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				return false;
			}
		}
		return res != null && !res.isEmpty();
	}

	/**
	 * 파일 업로드
	 *
	 * @param dir  저장할 디렉토리
	 * @param file 저장할 파일
	 * @return 업로드 여부
	 */
	public boolean upload(String dir, File file) {
		boolean isUpload = false;
		SftpATTRS attrs;
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			channelSftp.cd(dir);
			channelSftp.put(in, file.getName());

			// 업로드했는지 확인
			if (this.exists(dir + "/" + file.getName())) {
				isUpload = true;
			}
		} catch (SftpException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return isUpload;
	}

	/**
	 * 파일 다운로드
	 *
	 * @param dir              다운로드 할 디렉토리
	 * @param downloadFileName 다운로드 할 파일
	 * @param path             다운로드 후 로컬에 저장될 경로(파일명)
	 */
	public void download(String dir, String downloadFileName, String path) {
		InputStream in = null;
		FileOutputStream out = null;
		try {
			channelSftp.cd(dir);
			in = channelSftp.get(downloadFileName);
		} catch (SftpException e) {
			e.printStackTrace();
		}

		try {
			out = new FileOutputStream(new File(path));
			int i;

			while ((i = in.read()) != -1) {
				out.write(i);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 연결 종료
	 */
	public void disconnection() {
		channelSftp.quit();
		session.disconnect();
	}
}