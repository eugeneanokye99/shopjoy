package com.shopjoy.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleCache<K,V> {
    private final Map<K,V> map = new ConcurrentHashMap<>();

    public void put(K k, V v) { map.put(k,v); }
    public V get(K k) { return map.get(k); }
    public void remove(K k) { map.remove(k); }
    public void clear() { map.clear(); }
}
