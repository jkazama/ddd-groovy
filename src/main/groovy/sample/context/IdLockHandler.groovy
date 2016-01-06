package sample.context

import java.util.concurrent.locks.ReentrantReadWriteLock

import sample.InvocationException

/**
 * ID単位のロックを表現します。
 * low: ここではシンプルに口座単位のIDロックのみをターゲットにします。
 * low: 通常はDBのロックテーブルに"for update"要求で悲観的ロックをとったりしますが、サンプルなのでメモリロックにしてます。
 *
 * @author jkazama
 */
@StaticComponent
class IdLockHandler {

    private Map<Serializable, ReentrantReadWriteLock> lockMap = new HashMap<>()

    /** IDロック上で処理を実行します。 */
    def <V> V lock(Serializable id, LockType lockType, Closure<V> clos) {
        lockType.isWrite() ? writeLock(id) : readLock(id)
        try {
            clos.call()
        } catch (RuntimeException e) {
            throw e
        } catch (Exception e) {
            throw new InvocationException("error.Exception", e)
        } finally {
            unlock(id)
        }
    }

    void writeLock(Serializable id) {
        if (!id) return
        synchronized (lockMap) {
            idLock(id).writeLock().lock()
        }
    }

    private ReentrantReadWriteLock idLock(Serializable id) {
        if (!lockMap.containsKey(id)) lockMap[id] = new ReentrantReadWriteLock()
        lockMap[id]
    }

    void readLock(Serializable id) {
        if (!id) return
        synchronized (lockMap) {
            idLock(id).readLock().lock()
        }
    }

    void unlock(Serializable id) {
        if (!id) return
        synchronized (lockMap) {
            def idLock = idLock(id)
            idLock.isWriteLockedByCurrentThread() ? idLock.writeLock().unlock() : idLock.readLock().unlock()
        }
    }
}

/** ロック種別を表現するEnum */
enum LockType {
    /** 読み取り専用ロック */
    READ,
    /** 読み書き専用ロック */
    WRITE

    boolean isRead() {
        !isWrite()
    }

    boolean isWrite() {
        this == WRITE
    }
}

