package com.myexample.blockchain;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.myexample.common.constant.Result;
import com.myexample.common.utils.SecurityUtil;
import com.myexample.common.utils.StringUtil;

public class Transaction implements Serializable {

    private static final long serialVersionUID = -4830434376199056194L;

    private String transactionId;
    private long timestamp;
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

    public Transaction(String senderAddress, String recipientAddress, float value, List<UTXO> inputs, List<UTXO> outputs) {
        this.timestamp = Instant.now().toEpochMilli();
        this.senderAddress = senderAddress;
        this.recipientAddress = recipientAddress;
        this.value = value;
        this.transactionId = calculateHash();
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public float getValue() {
        return value;
    }

    public List<UTXO> getInputs() {
        return Collections.unmodifiableList(inputs);
    }

    public List<UTXO> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    public String calculateHash() {
        return SecurityUtil.sha256(
            Long.toString(timestamp) + senderAddress + recipientAddress + Float.toString(value)
            );
    }
    
    public String marshalJson() {
        return StringUtil.toJson(this);
    }

    public String marshalJsonPrettyPrinting() {
        var gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        return gsonBuilder.toJson(this);
    }

    public Result processTransaction() {
        var senderUTXOs = SBChain.uTXOPool
            .select(v -> v.belongsTo(senderAddress));

        // set inputs
        inputs = senderUTXOs.ceilingList(value);
        var inputsValue = inputs.stream()
            .map(UTXO::getValue)
            .reduce(0f, Float::sum);

        if (inputsValue < value) {
            System.out.println("# Not enough balance to send transaction. Transaction discarded.");
            return Result.TRANSACTION_NOTENOUGH_BALANCE;
        }
        if (inputsValue < SBChain.MINIMUM_TRANSACTION_VALUE) {
            System.out.println("# Transaction Inputs too small: " + inputsValue + ". Transaction discarded.");
            return Result.TRANSACTION_TOOSMALL_INPUTS;
        }

        // set outputs in this transaction
        outputs = List.of(
            new UTXO(recipientAddress, value, transactionId),
            new UTXO(senderAddress, inputsValue - value, transactionId));

        // update blockchain UTXOPool
        SBChain.uTXOPool.putAll(outputs);
        SBChain.uTXOPool.removeAll(inputs);

        return Result.TRANSACTION_SUCCESS;
    }

    public Result processGenesisTransaction() {
        // set outputs in this transaction
        outputs = List.of(new UTXO(recipientAddress, value, transactionId));

        // update blockchain UTXOPool
        SBChain.uTXOPool.putAll(outputs);

        return Result.TRANSACTION_SUCCESS;
    }
}
