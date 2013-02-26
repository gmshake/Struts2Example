package com.mkyong.common.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ActionSupport;

public class DownloadAction extends ActionSupport {
	private static final long serialVersionUID = 201301041328L;

	private static final Logger log = LoggerFactory
			.getLogger(DownloadAction.class);

	private static final Charset iso8859_1 = Charset.forName("ISO8859-1");
	private static final Charset utf_8 = Charset.forName("UTF-8");

	private InputStream fileInputStream;
	private String fileName;

	@Override
	public String execute() {
		if (this.fileName != null && isValidFileName(this.fileName)) {
			try {
				this.fileInputStream = new FileInputStream(new File(
						Const.parentDir, this.fileName));
				return SUCCESS;
			} catch (Exception e) {
				log.error("error open file: " + this.fileName, e);
			}
		}

		return ERROR;
	}

	private boolean isValidFileName(String path) {
		// single filename, does not permit / \ .. path
		if (path.contains("/") || path.contains("\\") || !path.endsWith(".txt"))
			return false;

		return true;
	}

	// getters and setters
	public void setFileName(String fileName) {
		log.debug("fileName from url: " + fileName);
		this.fileName = new String(fileName.getBytes(iso8859_1), utf_8);
		log.debug("corrected filename: " + this.fileName);
	}

	public String getFileName() {
		if (this.fileName == null)
			return "download";

		try {
			return URLEncoder.encode(this.fileName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("vm does not support UTF-8 encoding", e);
		}
	}

	public InputStream getFileInputStream() {
		return this.fileInputStream;
	}

}
