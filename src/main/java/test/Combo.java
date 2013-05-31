package test;

import java.util.ArrayList;
import java.util.Collection;

public class Combo {
	private final ArrayList<ExtendedLock> resource;

	private final ArrayList<ExtendedLock> locked;

	Combo(Collection<? extends ExtendedLock> resource) {
		if (resource == null)
			throw new NullPointerException();
		this.resource = new ArrayList<ExtendedLock>(resource);
		this.locked = new ArrayList<ExtendedLock>(resource.size());

		for (ExtendedLock l : this.resource) {
			l.addObserver(this);
		}
	}

	public synchronized void takeUp() throws InterruptedException {
		boolean done = false;

		while (true) {
			try {
				for (ExtendedLock l : this.resource) {
					if (l.tryLock()) {
						this.locked.add(l);
					}
				}
				if (this.resource.size() == this.locked.size()) {// all locked
					done = true;
				}
			} finally {
				if (!done) { // we release all lock and try again next time
					for (ExtendedLock l : this.locked) {
						l.unlock();
					}
					this.locked.clear();

					wait();
				} else
					break;
			}
		}
	}

	public synchronized void putDown() {// assume all locked
		for (ExtendedLock l : this.locked) {
			l.unlock();
		}

		for (ExtendedLock l : this.locked) {
			for (Object o : l.getObservers()) {
				synchronized (o) {
					o.notifyAll();
				}
			}
		}
		this.locked.clear();
	}

}
