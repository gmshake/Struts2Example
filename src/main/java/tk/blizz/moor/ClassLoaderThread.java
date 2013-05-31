package tk.blizz.moor;

import org.apache.log4j.Logger;

import tk.blizz.moor.loader.MoorClassLoader;

public class ClassLoaderThread extends Thread {
	private static final Logger log = Logger.getLogger(ClassLoaderThread.class);

	private Thread getThreadFromClassLoader(ClassLoader loader) {
		Thread thread = (Thread) Main.getInstanceFromClassLoader(
				"tk.blizz.moor.AppThread", loader);
		thread.setContextClassLoader(loader);
		return thread;
	}

	@Override
	public void run() {
		log.debug("Start run...");
		ClassLoader cl = Thread.currentThread().getClass().getClassLoader();
		ClassLoader cl1 = new MoorClassLoader("/tmp/shared", true);
		ClassLoader cl2 = new MoorClassLoader();

		Thread t1 = getThreadFromClassLoader(cl1);

		Thread t2 = getThreadFromClassLoader(cl2);

		t1.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		t2.start();

	}
}
