package com.bashkarsampath.streamers.pos.configurations;

import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class SftpConnector {
	@Getter(value = AccessLevel.PRIVATE)
	@Setter(value = AccessLevel.PRIVATE)
	private static String host;
	@Getter(value = AccessLevel.PRIVATE)
	@Setter(value = AccessLevel.PRIVATE)
	private static int port;
	@Getter(value = AccessLevel.PRIVATE)
	@Setter(value = AccessLevel.PRIVATE)
	private static String username;
	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private static String password;
	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private static String filedirecory;
	@Getter(value = AccessLevel.PRIVATE)
	private static final JSch jsch = new JSch();
	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private static Session session;
	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private static Channel channel;
	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private static ChannelSftp channelSftp;

	@Autowired
	private void injectSftpProperties(@Value("${sftp.host}") String host, @Value("${sftp.port}") int port,
			@Value("${sftp.username}") String username, @Value("${sftp.password}") String password,
			@Value("${sftp.file-directory}") String filedirectory) throws JSchException, SftpException {
		setHost(host);
		setPort(port);
		setUsername(username);
		setPassword(password);
		setFiledirecory(filedirectory);
	}

	private static void connect() throws JSchException, SftpException {
		if (getSession() == null || getChannel() == null || getChannelSftp() == null || !getSession().isConnected()
				|| !getChannel().isConnected() || !getChannelSftp().isConnected()) {
			setSession(getJsch().getSession(username, host, port));
			getSession().setPassword(password);
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			getSession().setConfig(config);
			long lStartTime = new Date().getTime();
			log.info("Connecting to the sftp server " + getHost() + " as " + getUsername());
			getSession().connect();
			long lEndTime = new Date().getTime();
			log.info("Connected to SFTP in : " + (lEndTime - lStartTime));
			setChannel(getSession().openChannel("sftp"));
			getChannel().connect();
			setChannelSftp((ChannelSftp) getChannel());
			try {
				getChannelSftp().cd(filedirecory);
			} catch (SftpException sftpException) {
				log.info("Failed to change the directory in sftp.");
				throw sftpException;
			}
		}
	}

	public static InputStream getFileAsInputStream(String sourceFileName) throws JSchException, SftpException {
		connect();
		return getChannelSftp().get(sourceFileName, new ProgressMonitor());
	}

	public static void getFile(String sourceFileName, String destinationFilePath) throws JSchException, SftpException {
		connect();
		getChannelSftp().get(sourceFileName, destinationFilePath, new ProgressMonitor());
	}

	@NoArgsConstructor
	private static class ProgressMonitor implements SftpProgressMonitor {
		private long max = 0;
		private long count = 0;
		private long percent = 0;

		public void init(int op, java.lang.String src, java.lang.String dest, long max) {
			this.max = max;
			log.info("Starting SFTP operation from " + src + " (size: " + max + " bytes) to " + dest);
		}

		public boolean count(long bytes) {
			this.count += bytes;
			long percentNow = this.count * 100 / max;
			if (percentNow > this.percent) {
				this.percent = percentNow;
				log.info("Progress: " + this.percent + "% (" + this.count + " bytes out of " + max + " bytes)");
			}
			return (true);
		}

		public void end() {
			log.info("Sftp operation complete: " + this.percent + "% processed (" + this.count + " bytes out of "
					+ this.max + " bytes)");
		}
	}
}
