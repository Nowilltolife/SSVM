package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.Memory;
import dev.xdark.ssvm.memory.MemoryManager;
import dev.xdark.ssvm.mirror.FieldInfo;
import dev.xdark.ssvm.mirror.JavaClass;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents VM object value.
 *
 * @author xDark
 */
public class ObjectValue implements Value {

	private final Lock lock;
	private final Condition signal;
	private final Memory memory;

	/**
	 * @param memory
	 * 		Object data.
	 */
	public ObjectValue(Memory memory) {
		this.memory = memory;
		var lock = new ReentrantLock();
		this.lock = lock;
		signal = lock.newCondition();
	}

	@Override
	public <T> T as(Class<T> type) {
		throw new IllegalStateException(type.toString());
	}

	@Override
	public boolean isWide() {
		return false;
	}

	@Override
	public boolean isUninitialized() {
		return false;
	}

	@Override
	public boolean isNull() {
		return false;
	}

	/**
	 * Returns object class.
	 *
	 * @return object class.
	 */
	public JavaClass getJavaClass() {
		return getMemoryManager().readClass(this);
	}

	/**
	 * Returns object data.
	 *
	 * @return object data.
	 */
	public Memory getMemory() {
		return memory;
	}

	/**
	 * Locks monitor
	 */
	public void monitorEnter() {
		lock.lock();
	}

	/**
	 * Unlocks monitor
	 */
	public void monitorExit() {
		lock.unlock();
	}

	/**
	 * Causes the current thread to wait until it is awakened,
	 * typically by being notified or interrupted.
	 *
	 * @param timeoutMillis
	 * 		The maximum time to wait, in milliseconds.
	 * @param nanos
	 * 		Additional time, in nanoseconds, in the range 0-999999 inclusive.
	 *
	 * @throws InterruptedException
	 * 		If Java thread was interrupted.
	 */
	public void vmWait(long timeoutMillis, int nanos) throws InterruptedException {
		signal.wait(timeoutMillis, nanos);
	}

	/**
	 * Wakes up a single thread that is waiting
	 * on this object's monitor.
	 */
	public void vmNotify() {
		signal.notify();
	}

	/**
	 * Wakes up all threads that are waiting
	 * on this object's monitor.
	 */
	public void vmNotifyAll() {
		signal.notifyAll();
	}

	protected MemoryManager getMemoryManager() {
		return memory.getMemoryManager();
	}
}