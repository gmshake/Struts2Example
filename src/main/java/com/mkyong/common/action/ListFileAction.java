package com.mkyong.common.action;

import java.io.File;
import java.io.FilenameFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ActionSupport;

public class ListFileAction extends ActionSupport {
	private static final long serialVersionUID = 201301041328L;

	private static final Logger log = LoggerFactory
			.getLogger(ListFileAction.class);

	private String[] files;

	private String context = "uuuu[]{uu:pp}";

	@Override
	public String execute() {
		log.debug("execute");
		this.files = checkFiles();
		return SUCCESS;
	}

	public String jsonListFiles() {
		log.debug("jsonListFiles");
		this.files = checkFiles();
		return SUCCESS;
	}

	private String[] checkFiles() {
		File dir = new File(Const.parentDir);
		return dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String arg1) {
				// accept only txt files
				if (arg1.endsWith(".txt"))
					return true;
				else
					log.debug("skip " + arg1);

				return false;
			}
		});

	}

	// getters and setters

	public String[] getFiles() {
		log.debug("get files");
		return this.files;
	}

	public String getContext() {
		log.debug("get context");
		return this.context;
	}

}
