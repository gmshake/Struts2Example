package tk.blizz.moor.loader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MoorClassLoader extends ClassLoader {
	private final String path;
	private final boolean useClassInPathOnly;

	public MoorClassLoader() {
		this(null);
	}

	public MoorClassLoader(String path) {
		this(path, false);
	}

	public MoorClassLoader(String path, boolean useClassInPathOnly) {
		this.path = path;
		this.useClassInPathOnly = useClassInPathOnly;
	}

	private final HashMap<String, Class<?>> classes = new HashMap<String, Class<?>>();

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {

		// First, check if the class has already been loaded
		Class<?> c = findLoadedClass(name);
		if (c == null) {
			c = myFindClass(name);
		}
		// if (c == null) {
		// c = getParent().loadClass(name);
		// }
		if (c == null) {
			c = super.loadClass(name, false);
		}
		if (resolve) {
			resolveClass(c);
		}
		return c;

	}

	public Class<?> myFindClass(String className) {
		System.out.println(this.toString() + " load: " + className);

		Class<?> result = this.classes.get(className); // checks in cached
														// classes
		if (result != null) {
			return result;
		}

		InputStream is = null;
		ByteArrayOutputStream byteStream = null;
		try {
			is = getFromPath(className);
			if (is == null && !this.useClassInPathOnly)
				is = getFromSystemClassPath(className);
			if (is == null)
				return null;
			byteStream = new ByteArrayOutputStream();
			int nextValue = is.read();
			while (-1 != nextValue) {
				byteStream.write(nextValue);
				nextValue = is.read();
			}

			byte[] classByte = byteStream.toByteArray();
			result = defineClass(className, classByte, 0, classByte.length,
					null);
			this.classes.put(className, result);
			return result;
		} catch (Exception e) {
			return null;
		} finally {
			if (byteStream != null)
				try {
					byteStream.close();
				} catch (IOException e) {
				}
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
		}
	}

	private InputStream getFromPath(String className) {
		if (this.path == null)
			return null;

		File f = new File(this.path);
		InputStream is = null;
		if (f.isDirectory()) {
			is = getFromDir(className, f);

			if (is != null)
				return is;

			File[] list = f.listFiles();
			if (list == null)
				return null;

			for (File l : list) {
				if (l.isFile() && l.getAbsolutePath().endsWith(".jar")) {
					is = getFromJar(className, l);
					if (is != null)
						return is;
				}
			}

		} else if (f.isFile() && this.path.endsWith(".jar")) {
			is = getFromJar(className, f);
			if (is != null)
				return is;
		}

		return null;
	}

	private InputStream getFromSystemClassPath(String className) {
		String[] cp = System.getProperty("java.class.path").split(":");
		if (cp != null) {
			for (String s : cp) {
				if (s == null || s.isEmpty())
					continue;
				File f = new File(s);
				InputStream is = null;
				if (f.isDirectory()) {
					is = getFromDir(className, f);

					if (is != null)
						return is;

					File[] list = f.listFiles();
					if (list == null)
						return null;
					for (File l : list) {
						if (l.isFile() && l.getAbsolutePath().endsWith(".jar")) {
							is = getFromJar(className, l);
							if (is != null)
								return is;
						}
					}

				} else if (f.isFile() && s.endsWith(".jar")) {
					is = getFromJar(className, f);
					if (is != null)
						return is;
				}
			}
		}
		return null;
	}

	private InputStream getFromDir(String className, File dir) {
		File target = new File(dir.getAbsolutePath(),
				classNameToFile(className));

		try {
			FileInputStream stream = new FileInputStream(target);
			System.out.println("found class " + className + " in dir: "
					+ dir.getAbsolutePath());
			return stream;
		} catch (Exception e) {
		}
		return null;
	}

	private InputStream getFromJar(String className, File jar) {
		try {
			JarFile jarFile = new JarFile(jar);
			JarEntry entry = jarFile.getJarEntry(classNameToFile(className));
			if (entry == null)
				return null;
			InputStream is = jarFile.getInputStream(entry);
			System.out.println("found class " + className + " in jar file: "
					+ jar.getAbsolutePath());
			return is;
		} catch (Exception e) {
		}
		return null;
	}

	private String classNameToFile(String className) {
		return className.replace('.', '/').concat(".class");
	}

	private String fileNameToClassName(String fileName) {
		return fileName.replaceAll("\\.class", "").replace('/', '.');
	}
}
