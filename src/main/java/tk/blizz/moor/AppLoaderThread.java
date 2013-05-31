package tk.blizz.moor;

import java.net.MalformedURLException;
import java.net.URL;

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
		log.debug("AppLoaderThread Start run...");
		ClassLoader cl = Thread.currentThread().getClass().getClassLoader();
		ClassLoader ctx = Thread.currentThread().getContextClassLoader();

		log.debug("AppLoaderThread's class loader: " + cl);
		log.debug("AppLoaderThread's context class loader: " + ctx);

		if (cl != ctx)
			throw new IllegalStateException();

		MoorClassLoader cl1;
		MoorClassLoader cl2;
		try {
			cl1 = new MoorClassLoader(cl, true, new URL("file", "",
					"/tmp/app1/shared/log4j-1.2.15.jar"));
			log.debug("set up app1's class loader: " + cl1 + " "
					+ "/tmp/app1/shared");

			cl2 = new MoorClassLoader(cl, true, new URL("file", "",
					"/tmp/app2/shared/log4j-1.2.15.jar"));
			log.debug("set up app2's class loader: " + cl2 + " "
					+ "/tmp/app2/shared");

			Thread t1 = getThreadFromClassLoader(cl1);
			Thread t2 = getThreadFromClassLoader(cl2);

			t1.setName("app1");
			t2.setName("app2");

			t1.start();
			t2.start();

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.debug("AppLoaderThread end !!!");
	}
}
