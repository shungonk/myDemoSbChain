package com.myexample.blockchain;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class Transaction {

    private String transactionId;
    private String senderAddress;
    private String recipientAddress;
    private float value;

    private List<UTXO> inputs = new ArrayList<>();
    private List<UTXO> outputs = new ArrayList<>();

    public static final String GENESIS_ID = "0"; // id of genesis transaction

    public Transaction(String transactionId, String senderAddress, String recipientAddress, float value) {
        this.transactionId = transactionId;
        this.senderAddress = senderAddress;
        this.recipientAddress = recipientAddress;
        this.value = value;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String marshalJson() {
        return new Gson().toJson(this);
    }

    public boolean isGenesis() {
        return transactionId.equals(GENESIS_ID);
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
