package tk.blizz.ssh.test;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestStringEncoding {
	private static final Logger log = LoggerFactory
			.getLogger(TestStringEncoding.class);

	@Test
	public void testEncode() {
		String s = "文本.txt";
		byte[] b = s.getBytes(Charset.forName("UTF-8"));
		System.out.println(Arrays.toString(b));
		b = s.getBytes(Charset.forName("GBK"));
		System.out.println(Arrays.toString(b));

		byte[] bb = new byte[] { -26, -106, -121, -26, -100, -84, 46, 116, 120,
				116 };

		String uu = new String(bb, Charset.forName("UTF-8"));
		System.out.println(uu);

		bb = new byte[] { -50, -60, -79, -66, 46, 116, 120, 116 };
		uu = new String(bb, Charset.forName("GBK"));
		System.out.println(uu);

		System.out.println(System.getProperty("file.encoding"));

		log.info("Hello world!");

	}
}
