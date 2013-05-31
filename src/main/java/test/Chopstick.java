package test;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Chopstick extends ReentrantLock implements ExtendedLock {
	private long id;
	private final AtomicBoolean inUse;
	private final Condition free;

	private final HashSet<Object> observers;

	Chopstick(long id) {
		this.id = id;
		this.inUse = new AtomicBoolean();
		this.free = newCondition();
		this.observers = new HashSet<Object>();
	}

	public long getId() {
		return this.id;
	}

	public boolean occupied() {
		return this.inUse.get();
	}

	public void occupy() {
		if (this.inUse.get()) {
			throw new IllegalStateException("Chopstick " + this.id
					+ " is occupied already");
		}

		this.inUse.set(true);
	}

	public Condition getCondition() {
		return this.free;
	}

	public void free() {
		if (!this.inUse.get()) {
			throw new IllegalStateException("Chopstick " + this.id
					+ " is not occupied");
		}
		this.inUse.set(false);
	}

	@Override
	public String toString() {
		return String.format("Chopstick %l", this.id);
	}

	@Override
	public void setObservers(Collection<?> observers) {
		synchronized (this.observers) {
			this.observers.clear();
			this.observers.addAll(observers);
		}
	}

	@Override
	public Collection<?> getObservers() {
		synchronized (this.observers) {
			return new HashSet<Object>(this.observers);
		}
	}

	@Override
	public void addObserver(Object observer) {
		synchronized (this.observers) {
			this.observers.add(observer);
		}
	}

	@Override
	public void removeObserver(Object observer) {
		synchronized (this.observers) {
			this.observers.remove(observer);
		}
	}
}
