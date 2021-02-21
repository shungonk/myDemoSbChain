package com.myexample.blockchain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.myexample.common.utils.SecurityUtil;

public class Transaction {

    private String transactionId;
    private Long timestamp;
    private String senderAddress;
    private String recipientAddress;
    private float value;

    private List<UTXO> inputs = new ArrayList<>();
    private List<UTXO> outputs = new ArrayList<>();

    public Transaction(String senderAddress, String recipientAddress, float value) {
        this.timestamp = Instant.now().toEpochMilli();
        this.senderAddress = senderAddress;
        this.recipientAddress = recipientAddress;
        this.value = value;
        this.transactionId = calculateHash();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String calculateHash() {
        return SecurityUtil.sha256(
            Long.toString(timestamp) + senderAddress + recipientAddress + Float.toString(value)
            );
    }
    
    public String marshalJson() {
        return new Gson().toJson(this);
    }

    public String marshalJsonPrettyPrinting() {
        var gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        return gsonBuilder.toJson(this);
    }

    public boolean processTransaction() {
        var senderUTXOs = SBChain.uTXOPool
            .select(v -> v.belongsTo(senderAddress));

        // set inputs
        inputs = senderUTXOs.ceilingList(value);
        var inputsValue = inputs.stream()
            .map(UTXO::getValue)
            .reduce(0f, Float::sum);

        if (inputsValue < value) {
            System.out.println("# Not enough balance to send transaction. Transaction discarded.");
            return false;
        }
        if (inputsValue < SBChain.MINIMUM_TRANSACTION_VALUE) {
            System.out.println("# Transaction Inputs too small: " + inputsValue + ". Transaction discarded.");
            return false;
        }

        // set outputs in this transaction
        outputs = List.of(
            new UTXO(recipientAddress, value, transactionId),
            new UTXO(senderAddress, inputsValue - value, transactionId));

        // update blockchain UTXOPool
        SBChain.uTXOPool.putAll(outputs);
        SBChain.uTXOPool.removeAll(inputs);

        return true;
    }

    public boolean processGenesisTransaction() {
        // set outputs in this transaction
        outputs = List.of(new UTXO(recipientAddress, value, transactionId));

        // update blockchain UTXOPool
        SBChain.uTXOPool.putAll(outputs);

        return true;
    }
}
