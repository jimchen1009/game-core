package com.game.common.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ConcurrentWeakReferenceMap<K, V> {

    private final Map<K, ConcurrentWeakReference<K, V>> key2References;
    private final ReferenceQueue<V> referenceQueue;
    private final Object synchronizedObject = new Object();

    public ConcurrentWeakReferenceMap() {
        this.key2References = new ConcurrentHashMap<>();
        this.referenceQueue = new ReferenceQueue<>();
    }

    public V get(K key) {
        removeAllQueuedValues();
        ConcurrentWeakReference<K, V> reference = key2References.get(key);
        /**
         * reference:
         * null，说明key已经被清理了
         * reference.getCache()为null，是因为清理的时候还有引用，清理完没引用了，所以get出来的值是null
         */
        return reference == null ? null : reference.get();
    }

    public void put(K key, V value) {
        removeAllQueuedValues();
        synchronized (synchronizedObject) {
            key2References.put(key, new ConcurrentWeakReference<>(key, value, referenceQueue));
        }
    }

    public V computeIfAbsent(K key, Function<? super K, ? extends V> function) {
        Objects.requireNonNull(function);
        V value = get(key);
        if (value == null){
            synchronized (synchronizedObject){
                value = get(key);
                if (value == null){
                    value = function.apply(key);
                    key2References.put(key, new ConcurrentWeakReference<>(key, value, referenceQueue));
                }
            }
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private void removeAllQueuedValues() {
        for (Reference<? extends V> poolValue; (poolValue = referenceQueue.poll()) != null; ) {
            synchronized (synchronizedObject) {
                ConcurrentWeakReference<K, V> removeReference = (ConcurrentWeakReference<K, V>) poolValue;
                ConcurrentWeakReference<K, V> currentReference = key2References.get(removeReference.getKey());

                /**
                 * removeReference 和 currentReference 可能是不同的对象
                 * 为了避免错删新put的value，只有currentReference引用为空时才删除key
                 */
                if (currentReference == null || currentReference.get() == null) {
                    key2References.remove(removeReference.getKey());
                }
            }
        }
    }

    class ConcurrentWeakReference<K, V> extends WeakReference<V> {
        private K key;

        public ConcurrentWeakReference(K key, V value, ReferenceQueue<V> queue) {
            super(value, queue);
            this.key = key;
        }

        public K getKey() {
            return key;
        }
    }
}
