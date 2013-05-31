package tk.blizz.moor;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import tk.blizz.moor.loader.MoorClassLoader;

public class Main implements Runnable {
	public static AtomicInteger appCount = new AtomicInteger();

	public Main() {
		super();
	}

	@Override
	public void run() {
		final Logger log = Logger.getLogger(Main.class);
		log.info("Moor start...");
		log.info("class loader: " + this.getClass().getClassLoader());
		log.info("context class loader: "
				+ Thread.currentThread().getContextClassLoader());

		final ClassLoader moor = this.getClass().getClassLoader();

		ThreadGroup apps = new ThreadGroup("Apps");
		Thread p = new Thread(apps, new Runnable() {
			private final Logger log = Logger.getLogger(this.getClass());

			@Override
			public void run() {
				log.info("start up app loader thread...");

				log.info("class loader: " + this.getClass().getClassLoader());
				log.info("context class loader: "
						+ Thread.currentThread().getContextClassLoader());

				MoorClassLoader loader;
				try {
					loader = new MoorClassLoader(Thread.currentThread()
							.getContextClassLoader(), true, new URL("file", "",
							"/tmp/shared"));

					log.info("set up AppLoaderThread class path: " + loader
							+ " " + "/tmp/shared");

					Thread t = (Thread) Utils.getInstanceFromClassLoader(
							"tk.blizz.moor.AppLoaderThread", loader);

					t.setContextClassLoader(loader);

					t.start();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, "AppLoader");
		p.setContextClassLoader(moor);

		Thread monitor = new Thread(new Runnable() {
			private final Logger log = Logger.getLogger(this.getClass());

			@Override
			public void run() {
				log.info("start monitor thread...");

				log.info("class loader: " + this.getClass().getClassLoader());
				log.info("context class loader: "
						+ Thread.currentThread().getContextClassLoader());

				ThreadGroup group = Thread.currentThread().getThreadGroup();
				synchronized (group) {
					while (true) {
						try {
							log.info("wait signals");
							group.wait(1000);
							if (appCount.get() == 0)
								break;
						} catch (InterruptedException e) {
						}
					}
				}
				log.info("monitor thread end!!! all apps unloaded");
			}
		}, "Monitor");
		monitor.setContextClassLoader(moor);

		monitor.start();

		p.start();
	}

	public static void main(String[] args) throws MalformedURLException {
		System.out.println("bootstrap start...");

		System.out.println("class loader: " + Main.class.getClassLoader());
		System.out.println("context class loader: "
				+ Thread.currentThread().getContextClassLoader());

		File f = new File("/tmp/moor");

		File[] list = f.listFiles();
		URL[] p = new URL[list.length + 1];

		for (int i = 0; i < list.length; i++) {
			p[i] = new URL("file", "", list[i].getAbsolutePath() + "/");
		}
		p[list.length] = new URL("file", "", f.getAbsolutePath() + "/");

		final MoorClassLoader moor = new MoorClassLoader(true, p);

		System.out.println("set up Moor class path: " + moor + " "
				+ f.getAbsolutePath());

		Runnable self = (Runnable) Utils.getInstanceFromClassLoader(
				"tk.blizz.moor.Main", moor);

		ThreadGroup tg = new ThreadGroup("moor");
		Thread thread = new Thread(tg, self, "Moor");
		thread.setContextClassLoader(moor);
		thread.start();
	}
}
