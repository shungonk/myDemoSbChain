package com.myexample.transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class UTXOPool {

    private HashMap<String, UTXO> pool = new HashMap<>();

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

    public List<UTXO> ceilingList(float value) {
        var total = 0f;
        var ceiling = new ArrayList<UTXO>();
        for (var uTXO: values()) {
            total += uTXO.getValue();
            ceiling.add(uTXO);
            if (total > value) break;
        }
        return ceiling;
    }
}
