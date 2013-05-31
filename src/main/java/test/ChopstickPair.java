package test;

import java.util.HashSet;

public class ChopstickPair extends Combo {
	private Chopstick left;
	private Chopstick right;

	ChopstickPair(Chopstick left, Chopstick right) {
		super(fromLeftRight(left, right));
		this.left = left;
		this.right = right;
	}

	private static HashSet<Chopstick> fromLeftRight(Chopstick left,
			Chopstick right) {
		HashSet<Chopstick> s = new HashSet<Chopstick>();
		s.add(left);
		s.add(right);
		return s;
	}

	@Override
	public synchronized void takeUp() throws InterruptedException {
		super.takeUp();
		this.left.occupy();
		this.right.occupy();
	}

	@Override
	public synchronized void putDown() {
		this.left.free();
		this.right.free();
		super.putDown();
	}

	@Override
	public String toString() {
		return this.left.getId() + " " + this.right.getId();
	}

}
