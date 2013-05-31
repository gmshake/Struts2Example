package test;

public class Philospher implements Runnable {
	private static final int PHILOSPHERCOUNT = 101;

	private ChopstickPair chopstick;

	private final long id;

	public Philospher(int number, Chopstick left, Chopstick right) {
		this.id = number;
		this.chopstick = new ChopstickPair(left, right);
	}

	public long getId() {
		return this.id;
	}

	private synchronized void takeup() throws InterruptedException {
		this.chopstick.takeUp();

		System.out.println("哲学家" + Thread.currentThread().getName()
				+ "拿到了两支筷子,请进餐 " + this.chopstick);

	}

	private void putdown() throws InterruptedException {
		this.chopstick.putDown();

		System.out.println("哲学家" + Thread.currentThread().getName()
				+ "放下了两支筷子,就餐完毕 " + this.chopstick);

	}

	@Override
	public void run() {
		while (true) {
			try {
				takeup();
				eating();
				putdown();
				thinking();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void thinking() {
		System.out.println("哲学家" + Thread.currentThread().getName() + "正在思考");
		try {
			Thread.sleep((int) (500 + Math.random() * 1000));
		} catch (InterruptedException e) {
		}
	}

	public void eating() {
		System.out.println("哲学家" + Thread.currentThread().getName() + "正在进餐");
		try {
			Thread.sleep((int) (500 + Math.random() * 1000));
		} catch (InterruptedException e) {
		}
	}

	public static void main(String[] args) {
		Chopstick[] chops = new Chopstick[PHILOSPHERCOUNT];
		Philospher[] ps = new Philospher[PHILOSPHERCOUNT];
		for (int i = 0; i < PHILOSPHERCOUNT; i++) {
			chops[i] = new Chopstick(i);
		}

		for (int i = 0; i < PHILOSPHERCOUNT; i++) {
			int j = (i + 1) % PHILOSPHERCOUNT;
			ps[i] = new Philospher(i, chops[i], chops[j]);
		}

		for (int i = 0; i < PHILOSPHERCOUNT; i++) {
			new Thread(ps[i], "Philospher-" + i).start();
		}

	}
}
