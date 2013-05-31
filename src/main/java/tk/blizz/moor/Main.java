package tk.blizz.moor;

import java.io.File;

import org.apache.log4j.Logger;

import tk.blizz.moor.loader.MoorClassLoader;

public class Main {
	private static Logger log = Logger.getLogger(Main.class);

	public static Object getInstanceFromClassLoader(String className,
			ClassLoader loader) {
		Object t;
		Class<?> threadClazz;
		try {
			threadClazz = loader.loadClass(className);
			t = threadClazz.newInstance();

		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		return t;
	}

	public static void main(String[] args) {
		File f = new File("target/classes/shared");

		log.info("shared class path: " + f.getAbsolutePath());

		final MoorClassLoader shared = new MoorClassLoader(f.getAbsolutePath());

		Thread p = new Thread(new ThreadGroup("moor"), new Runnable() {
			@Override
			public void run() {
				ClassLoader cl = Thread.currentThread().getContextClassLoader();

				Thread t = (Thread) getInstanceFromClassLoader(
						"tk.blizz.moor.ClassLoaderThread", shared);

				if (t.getContextClassLoader() != cl)
					throw new IllegalStateException("wrong class loader");
				t.start();
			}
		});
		p.setContextClassLoader(shared);
		p.start();
	}
}
