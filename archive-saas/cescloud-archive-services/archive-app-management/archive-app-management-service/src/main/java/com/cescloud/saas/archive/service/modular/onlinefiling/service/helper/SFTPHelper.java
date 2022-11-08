package com.cescloud.saas.archive.service.modular.onlinefiling.service.helper;

import com.jcraft.jsch.*;

import java.io.*;
import java.util.Properties;


public class SFTPHelper {
	private ChannelSftp sftp = null;

	private Session sshSession = null;

	private String username;

	private String password;

	private String host;

	private int port;

	public SFTPHelper(String username, String password, String host, int port) {
		this.username = username;
		this.password = password;
		this.host = host;
		this.port = port;
	}

	/**
	 * 连接sftp服务器
	 *
	 * @return ChannelSftp sftp连接实例
	 */
	public ChannelSftp connect() {
		info("-->sftp连接开始>>>>>> " + host + ":" + port + " >>>username=" + username);
		JSch jsch = new JSch();
		try {
			jsch.getSession(username, host, port);
			sshSession = jsch.getSession(username, host, port);
			sshSession.setPassword(password);
			Properties properties = new Properties();
			properties.put("StrictHostKeyChecking", "no");
			sshSession.setConfig(properties);
			sshSession.connect();
			Channel channel = sshSession.openChannel("sftp");
			channel.connect();
			sftp = (ChannelSftp) channel;
			info(" ftp Connected to " + host + ":" + port);
		} catch (JSchException e) {
			throw new RuntimeException("sftp连接失败", e);
		}
		return sftp;
	}

	/**
	 * 下载单个文件，如果指定文件名，则下载到文件名否则保持原有文件名
	 *
	 * @param remoteFilePath    远程文件路径 /tmp/xxx.txt || xxx.txt.zip
	 * @param localFilePath     本地文件路径 如 D:\\xxx.txt
	 * @return 下载的文件
	 */
	public File downloadFile(String remoteFilePath, String localFilePath) {
		info(">>>>>>>>>downloadFile--ftp下载文件" + remoteFilePath + "开始>>>>>>>>>>>>>");
		connect();
		String remoteFileName = "";
		// 远端目录确定以 / 作为目录格式
		String rFileSeparator = "/";
		int rDirNameSepIndex = remoteFilePath.lastIndexOf(rFileSeparator) + 1;
		String rDir = remoteFilePath.substring(0, rDirNameSepIndex);
		remoteFileName = remoteFilePath.substring(rDirNameSepIndex);
		if(localFilePath.endsWith(File.separator)) {
			localFilePath = localFilePath + (localFilePath.endsWith(File.separator) ? remoteFileName : "/" + remoteFileName);
		}
		File file = null;
		OutputStream output = null;
		try {
			file = new File(localFilePath);
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			sftp.cd(rDir);
			output = new FileOutputStream(file);
			sftp.get(remoteFileName, output);
			info("===DownloadFile:" + remoteFileName + " success from sftp.");
		} catch (SftpException e) {
			error("ftp下载文件失败", e);
		} catch (FileNotFoundException e) {
			error("本地目录异常，请检查" + file.getPath(), e);
		} catch (IOException e) {
			error("创建本地文件失败" + file.getPath(), e);
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			disconnect();
		}

		info(">>>>>>>>>downloadFile--ftp下载文件结束>>>>>>>>>>>>>");
		return file;
	}

	/**
	 * 关闭连接
	 */
	public void disconnect() {
		if (this.sftp != null) {
			if (this.sftp.isConnected()) {
				this.sftp.disconnect();
				this.sftp = null;
				info("sftp 连接已关闭！");
			}
		}
		if (this.sshSession != null) {
			if (this.sshSession.isConnected()) {
				this.sshSession.disconnect();
				this.sshSession = null;
				info("sshSession 连接已关闭！");
			}
		}
	}
	private void info(String msg) {
		System.out.println("info: " + msg);
	}

	private void error(String msg) {
		error(msg, null);
	}

	private void error(String msg, Throwable e) {
		System.out.println("error: " + msg);
		if(e != null) {
			e.printStackTrace();
		}
	}
}
