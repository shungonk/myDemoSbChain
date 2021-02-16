package com.myexample.blockchain;

import java.util.ArrayList;
import java.util.List;

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

    public boolean isGenesis() {
        return transactionId.equals(GENESIS_ID);
    }

    public boolean doProcess() {
        return isGenesis()
            ? processGenesisTransaction()
            : processTransaction();
    }

    private boolean processTransaction() {
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

        // set outputs in transaction
        outputs = List.of(
            new UTXO(recipientAddress, value, transactionId),
            new UTXO(senderAddress, inputsValue - value, transactionId));

        // update UTXOPool of blockchain
        SBChain.uTXOPool.putAll(outputs);
        SBChain.uTXOPool.removeAll(inputs);

        return true;
    }

    private boolean processGenesisTransaction() {
        // set outputs in transaction
        outputs = List.of(new UTXO(recipientAddress, value, transactionId));

        // update UTXOPool of blockchain
        SBChain.uTXOPool.putAll(outputs);

        return true;
    }
}
