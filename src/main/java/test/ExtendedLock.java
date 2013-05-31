package test;

import java.util.Collection;
import java.util.concurrent.locks.Lock;

public interface ExtendedLock extends Lock {
	void setObservers(Collection<?> observers);

	void addObserver(Object observer);

	void removeObserver(Object observer);

	Collection<?> getObservers();

}
