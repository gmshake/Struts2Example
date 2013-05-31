package tk.blizz.moor.loader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class MoorClassLoader extends ClassLoader {
	private boolean debugEnabled;
	private boolean traceEnabled;
	private final URL[] path;
	private final boolean parentLast;

	private final HashMap<String, Class<?>> classes;

	public MoorClassLoader() {
		this(getSystemClassLoader());
	}

	public MoorClassLoader(URL path) {
		this(new URL[] { path });
	}

	public MoorClassLoader(URL[] path) {
		this(false, path);
	}

	public MoorClassLoader(boolean parentLast, URL path) {
		this(parentLast, new URL[] { path });
	}

	public MoorClassLoader(boolean parentLast, URL[] path) {
		this(getSystemClassLoader(), parentLast, path);
	}

	public MoorClassLoader(ClassLoader parent) {
		this(parent, false);
	}

	public MoorClassLoader(ClassLoader parent, boolean parentLast) {
		this(parent, parentLast, (URL[]) null);
	}

	public MoorClassLoader(ClassLoader parent, boolean parentLast, URL path) {
		this(parent, parentLast, new URL[] { path });
	}

	public MoorClassLoader(ClassLoader parent, boolean parentLast, URL[] path) {
		super(parent);

		ArrayList<URL> systemClassPath = parsePath(System.getProperty(
				"java.class.path").split(":"));
		ArrayList<URL> userClassPath = parsePath(path);

		ArrayList<URL> pathList = new ArrayList<URL>(systemClassPath.size()
				+ userClassPath.size());

		if (parentLast) {
			pathList.addAll(userClassPath);
			pathList.addAll(systemClassPath);
		} else {
			pathList.addAll(systemClassPath);
			pathList.addAll(userClassPath);
		}

		path = new URL[pathList.size()];

		this.path = pathList.toArray(path);
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
			URL u = getFromPath(this.path, classNameToFile(className));
			is = u.openStream();

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
		if (parentLast) {
			URL u = getFromPath(this.path, name);
			return u != null ? u : super.findResource(name);
		} else {
			URL u = super.findResource(name);
			return u != null ? u : getFromPath(this.path, name);
		}
	}

	@Override
	protected Enumeration<URL> findResources(String name) throws IOException {
		return new ResourcesEnumeration(this.path, super.findResources(name),
				name, parentLast);
	}

	private ArrayList<URL> parsePath(String[] cp) {
		ArrayList<URL> list;
		if (cp != null) {
			list = new ArrayList<URL>(cp.length);
			for (String s : cp) {
				if (s == null)
					continue;

				if (s.isEmpty())
					continue;

				File f = new File(s);
				try {
					if (f.isDirectory())
						list.add(new URL("file", "", f.getAbsolutePath() + "/"));
					else if (f.isFile() && f.getAbsolutePath().endsWith(".jar"))
						list.add(new URL("jar", "", "file://"
								+ f.getAbsolutePath() + "!/"));
				} catch (MalformedURLException e) {
					if (traceEnabled)
						e.printStackTrace();
				}
			}
		} else
			list = new ArrayList<URL>(0);
		return list;
	}

	private ArrayList<URL> parsePath(URL[] cp) {
		ArrayList<URL> list;
		if (cp != null) {
			list = new ArrayList<URL>(cp.length);
			for (URL s : cp) {
				if (s == null)
					continue;

				if ("file".equalsIgnoreCase(s.getProtocol())) {
					File f = new File(s.getFile());
					try {
						if (f.isDirectory())
							list.add(new URL("file", "", f.getAbsolutePath()
									+ "/"));
						else if (f.isFile()
								&& f.getAbsolutePath().endsWith(".jar"))
							list.add(new URL("jar", "", s.toExternalForm()
									+ "!/"));
					} catch (MalformedURLException e) {
						if (traceEnabled)
							e.printStackTrace();
					}
				} else {
					list.add(s);
				}
			}
		} else
			list = new ArrayList<URL>(0);
		return list;
	}

	private URL getFromPath(URL[] urls, String resourceName) {
		for (URL s : urls) {
			URL is = null;
			if ("file".equalsIgnoreCase(s.getProtocol())) {
				File f = new File(s.getFile());
				if (f.isDirectory())
					is = getFromClassDir(s, resourceName);

			} else if ("jar".equalsIgnoreCase(s.getProtocol())) {
				is = getFromJar(s, resourceName);
			} else {
				// FIXME
				try {
					is = new URL(s, resourceName);
				} catch (MalformedURLException e) {
					if (traceEnabled)
						e.printStackTrace();
				}
			}

			if (is != null) {
				if (debugEnabled)
					System.out.println(this.toString() + " found resource "
							+ resourceName + " in " + s);
				return is;
			}
		}

		return null;
	}

	private URL getFromClassDir(URL dir, String resourceName) {
		File target = new File(dir.getFile(), resourceName);

		if (target.exists())
			try {
				return new URL(dir, resourceName);
			} catch (MalformedURLException e) {
				if (traceEnabled)
					e.printStackTrace();
			}

		return null;
	}

	private URL getFromJar(URL jar, String resourceName) {
		try {
			URL u = new URL(jar, resourceName);
			InputStream is = null;
			try {
				is = u.openStream();
				if (is != null)
					return u;
			} finally {
				if (is != null)
					try {
						is.close();
					} catch (IOException e) {
					}
			}
		} catch (MalformedURLException e) {
			if (traceEnabled)
				e.printStackTrace();
		} catch (IOException e) {
		}
		return null;
	}

	private URL getFromLibDir(URL dir, String resourceName) {
		for (File f : new File(dir.getFile()).listFiles()) {
			if (f.isFile() && f.getAbsolutePath().endsWith(".jar")) {
				URL jar;
				try {
					jar = new URL("jar", "", "file://" + f.getAbsolutePath()
							+ "!/");
					URL newJar = getFromJar(jar, resourceName);
					if (newJar != null)
						return newJar;
				} catch (MalformedURLException e) {
					if (traceEnabled)
						e.printStackTrace();
				}
			}

		}

		return null;
	}

	private String classNameToFile(String className) {
		return className.replace('.', '/').concat(".class");
	}

	public boolean isDebugEnabled() {
		return debugEnabled;
	}

	public void setDebugEnabled(boolean debugEnabled) {
		this.debugEnabled = debugEnabled;
	}

	private class ResourcesEnumeration implements Enumeration<URL> {
		private final Iterator<URL> it;
		private final Enumeration<URL> supers;
		private final boolean parentLast;

		ResourcesEnumeration(URL[] urls, Enumeration<URL> supers, String name,
				boolean parentLast) {
			this.supers = supers;
			this.parentLast = parentLast;
			ArrayList<URL> u = getFromPath(urls, name);
			this.it = u.iterator();
		}

		@Override
		public boolean hasMoreElements() {
			if (this.parentLast) {
				return this.it.hasNext() ? this.it.hasNext() : supers
						.hasMoreElements();
			} else {
				return supers.hasMoreElements() ? supers.hasMoreElements()
						: this.it.hasNext();
			}
		}

		@Override
		public URL nextElement() {
			if (this.parentLast) {
				try {
					return this.it.next();
				} catch (NoSuchElementException e) {
					return supers.nextElement();
				}
			} else {
				try {
					return supers.nextElement();
				} catch (NoSuchElementException e) {
					return this.it.next();
				}
			}
		}

		private ArrayList<URL> getFromPath(URL[] urls, String resourceName) {
			ArrayList<URL> list = new ArrayList<URL>(urls.length);
			for (URL s : urls) {
				URL is = null;
				if ("file".equalsIgnoreCase(s.getProtocol())) {
					File f = new File(s.getFile());
					if (f.isDirectory())
						is = getFromClassDir(s, resourceName);

				} else if ("jar".equalsIgnoreCase(s.getProtocol())) {
					is = getFromJar(s, resourceName);
				} else {
					// FIXME
					try {
						is = new URL(s, resourceName);
					} catch (MalformedURLException e) {
						if (traceEnabled)
							e.printStackTrace();
					}
				}

				if (is != null)
					list.add(is);
			}

			return list;
		}

		private URL getFromClassDir(URL dir, String resourceName) {
			File target = new File(dir.getFile(), resourceName);

			if (target.exists())
				try {
					return new URL(dir, resourceName);
				} catch (MalformedURLException e) {
					if (traceEnabled)
						e.printStackTrace();
				}

			return null;
		}

		private URL getFromJar(URL jar, String resourceName) {
			try {
				URL u = new URL(jar, resourceName);
				InputStream is = null;
				try {
					is = u.openStream();
					if (is != null)
						return u;
				} finally {
					if (is != null)
						try {
							is.close();
						} catch (IOException e) {
						}
				}
			} catch (MalformedURLException e) {
				if (traceEnabled)
					e.printStackTrace();
			} catch (IOException e) {
			}
			return null;
		}
	}
}
