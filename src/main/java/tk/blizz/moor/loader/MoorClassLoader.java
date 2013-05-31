package tk.blizz.moor.loader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MoorClassLoader extends ClassLoader {
	private boolean debugEnabled;
	private final String[] path;
	private final ArrayList<URL> urls;
	private final boolean parentLast;

	private final HashMap<String, Class<?>> classes;

	public MoorClassLoader() {
		this(getSystemClassLoader());
	}

	public MoorClassLoader(String path) {
		this(new String[] { path });
	}

	public MoorClassLoader(String[] path) {
		this(false, path);
	}

	public MoorClassLoader(boolean parentLast, String path) {
		this(parentLast, new String[] { path });
	}

	public MoorClassLoader(boolean parentLast, String[] path) {
		this(getSystemClassLoader(), parentLast, path);
	}

	public MoorClassLoader(ClassLoader parent) {
		this(parent, false);
	}

	public MoorClassLoader(ClassLoader parent, boolean parentLast) {
		this(parent, parentLast, (String[]) null);
	}

	public MoorClassLoader(ClassLoader parent, boolean parentLast, String path) {
		this(parent, parentLast, new String[] { path });
	}

	public MoorClassLoader(ClassLoader parent, boolean parentLast, String[] path) {
		super(parent);

		ArrayList<String> systemClassPath = parsePath(System.getProperty(
				"java.class.path").split(":"));
		ArrayList<String> userClassPath = parsePath(path);

		ArrayList<String> pathList = new ArrayList<String>(
				systemClassPath.size() + userClassPath.size());
		pathList.addAll(systemClassPath);
		pathList.addAll(userClassPath);

		path = new String[pathList.size()];

		this.path = pathList.toArray(path);
		this.urls = getUrls(this.path);
		this.parentLast = parentLast;
		this.classes = new HashMap<String, Class<?>>();
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {

		Class<?> c = findLoadedClass(name);
		if (c == null) {
			if (this.parentLast) {
				try {
					c = findClass(name);
				} catch (ClassNotFoundException e) {
					// ClassNotFoundException thrown if class not found
					// from the non-null parent class loader
				}
			}

			if (c == null) {
				try {
					if (getParent() != null) {
						c = getParent().loadClass(name);
					} else {
						c = getSystemClassLoader().loadClass(name);
					}
				} catch (ClassNotFoundException e) {
					// ClassNotFoundException thrown if class not found
					// from the non-null parent class loader
				}
			}

			if (c == null && !this.parentLast) {
				c = findClass(name);
			}
		}
		if (resolve) {
			resolveClass(c);
		}
		return c;
	}

	@Override
	public Class<?> findClass(String className) throws ClassNotFoundException {
		if (isDebugEnabled())
			System.out.println(this.toString() + " load: " + className);

		Class<?> result = this.classes.get(className); // checks in cached
														// classes
		if (result != null) {
			return result;
		}

		// FIXME: need this??
		// try {
		// return findSystemClass(className);
		// } catch (Exception e) {
		// }

		InputStream is = null;
		ByteArrayOutputStream byteStream = null;
		try {
			is = getFromPath(className);

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
			throw new ClassNotFoundException(className);
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

	@Override
	protected URL findResource(String name) {
		return super.findResource(name);
	}

	@Override
	protected Enumeration<URL> findResources(String name) throws IOException {
		return super.findResources(name);
	}

	private InputStream getFromPath(String className) {
		for (String s : this.path) {
			InputStream is = null;
			File f = new File(s);
			if (f.isDirectory()) {
				is = getFromDir(className, f);
			} else if (f.isFile() && f.getAbsolutePath().endsWith(".jar")) {
				is = getFromJar(className, f);
			}

			if (is != null)
				return is;
		}

		return null;
	}

	private ArrayList<String> parsePath(String[] cp) {
		ArrayList<String> list;
		if (cp != null) {
			list = new ArrayList<String>(cp.length);
			for (String s : cp) {
				if (s == null)
					continue;
				if (s.isEmpty())
					continue;
				File f = new File(s);
				if (f.isDirectory())
					list.add(f.getAbsolutePath());
				else if (f.isFile() && f.getAbsolutePath().endsWith(".jar"))
					list.add(f.getAbsolutePath());
			}
		} else
			list = new ArrayList<String>(0);
		return list;
	}

	private InputStream getFromDir(String className, File dir) {
		File target = new File(dir.getAbsolutePath(),
				classNameToFile(className));

		try {
			FileInputStream stream = new FileInputStream(target);
			if (isDebugEnabled())
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
			if (isDebugEnabled())
				System.out.println("found class " + className
						+ " in jar file: " + jar.getAbsolutePath());
			return is;
		} catch (Exception e) {
		}
		return null;
	}

	private ArrayList<URL> getUrls(String[] path) {
		ArrayList<URL> urls = new ArrayList<URL>(path.length + 10);
		for (String s : path) {
			try {
				urls.add(new URL("file", "", s));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

		return urls;
	}

	private String classNameToFile(String className) {
		return className.replace('.', '/').concat(".class");
	}

	private String fileNameToClassName(String fileName) {
		return fileName.replaceAll("\\.class", "").replace('/', '.');
	}

	public boolean isDebugEnabled() {
		return debugEnabled;
	}

	public void setDebugEnabled(boolean debugEnabled) {
		this.debugEnabled = debugEnabled;
	}

	private class MoorResourcesEnumeration implements Enumeration<URL> {
		private final Iterator<URL> it;
		private final Enumeration<URL> supers;

		MoorResourcesEnumeration(Iterator<URL> it, Enumeration<URL> supers) {
			this.it = it;
			this.supers = supers;
		}

		@Override
		public boolean hasMoreElements() {
			return this.it.hasNext();
		}

		@Override
		public URL nextElement() {
			return this.it.next();
		}

	}
}
