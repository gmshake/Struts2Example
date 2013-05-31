package tk.blizz.moor;

public class Utils {
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
}
