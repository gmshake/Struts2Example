package tk.blizz.moor;

import org.apache.log4j.Logger;

import tk.blizz.moor.loader.MoorClassLoader;

public class AppLoaderThread extends Thread {
	private static final Logger log = Logger.getLogger(AppLoaderThread.class);

	private Thread getThreadFromClassLoader(ClassLoader loader) {
		Thread thread = (Thread) Utils.getInstanceFromClassLoader(
				"tk.blizz.moor.AppThread", loader);
		thread.setContextClassLoader(loader);
		return thread;
	}

	@Override
	public void run() {
		log.debug("ClassLoaderThread Start run...");
		ClassLoader cl = Thread.currentThread().getClass().getClassLoader();

		log.debug("ClassLoaderThread's class loader: " + cl);
		log.debug("ClassLoaderThread's context class loader: "
				+ Thread.currentThread().getContextClassLoader());
		if (cl != Thread.currentThread().getContextClassLoader())
			throw new IllegalStateException();

		ClassLoader cl1 = new MoorClassLoader(cl, true, "/tmp/app1/shared");
		ClassLoader cl2 = new MoorClassLoader(cl, false, "/tmp/app2/shared");

		Thread t1 = getThreadFromClassLoader(cl1);

		Thread t2 = getThreadFromClassLoader(cl2);

		t1.start();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		t2.start();

		ThreadGroup group = Thread.currentThread().getThreadGroup();
		synchronized (group) {
			group.notifyAll();
		}
		log.debug("ClassLoaderThread end !!!");

	}
}
