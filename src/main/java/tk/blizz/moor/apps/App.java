package tk.blizz.moor.apps;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

public class App {
	private static final AtomicLong id_generator = new AtomicLong();

	// private static final Logger log = LoggerFactory.getLogger(App.class);
	private static final Logger log = Logger.getLogger(App.class);

	private final long id;

	App() {
		this.id = id_generator.getAndIncrement();
	}

	@Override
	public String toString() {
		return this.getClass().getClassLoader() + "#" + super.toString() + "@"
				+ this.id;
	}

	public void run() {
		System.out.println("logger's class loader: "
				+ log.getClass().getClassLoader());
		System.out.println("app" + this.id + "'s class loader: "
				+ this.getClass().getClassLoader());
		Random rand = new Random();
		for (int i = 0; i < 3; i++) {
			log.info(this.toString());
			try {
				Thread.sleep(1000 + rand.nextInt(2000));
			} catch (InterruptedException e) {
			}
		}
	}

	public static void main(String[] args) {
		new App().run();
		new App().run();
	}
}
