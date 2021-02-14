package com.myexample.blockchain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UTXOPool {

    private Map<String, UTXO> pool = new ConcurrentHashMap<>();

    public Collection<UTXO> values() {
        return pool.values();
    }

    public UTXO put(UTXO uTXO) {
        return pool.put(uTXO.getId(), uTXO);
    }
    
    public void putAll(Collection<UTXO> uTXOs) {
        uTXOs.forEach(this::put);
    }

    public UTXO remove(UTXO uTXO) {
        return pool.remove(uTXO.getId());
    }
    
    public void removeAll(Collection<UTXO> uTXOs) {
        uTXOs.forEach(this::remove);
    }

    public float totalValue() {
        return values().stream()
            .map(UTXO::getValue)
            .reduce(0f, Float::sum);
    }

    public UTXOPool select(Predicate<UTXO> matcher) {
        var result = new UTXOPool();
        var newPool =  values().stream()
            .filter(matcher)
            .collect(Collectors.toMap(UTXO::getId, v -> v, (v1 ,v2) -> v1, HashMap::new));
        result.pool = newPool;
        return result;
    }

    public List<UTXO> ceilingList(float value) {
        var total = 0f;
        var ceilingList = new ArrayList<UTXO>();
        for (var uTXO: values()) {
            total += uTXO.getValue();
            ceilingList.add(uTXO);
            if (total > value) break;
        }
        return ceilingList;
    }
}
