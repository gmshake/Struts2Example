package com.mkyong.common.action;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Const extends Object {
	public static final String parentDir = "/tmp";
	public static final AtomicReference<String> ab = null;
	private final Thread t = null;
	
	Const() {
		super();
	}
	
	public static void main(String[] args) {
		Const c = new Const();
		
		ClassLoader cl = c.getClass().getClassLoader();
		
		ClassLoader ctx = Thread.currentThread().getContextClassLoader();
		
		System.out.println(cl);
	}

}
