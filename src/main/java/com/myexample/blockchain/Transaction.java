package com.myexample.blockchain;

import java.util.ArrayList;
import java.util.List;

import com.myexample.utils.CryptoUtil;

public class Transaction {

    private String transactionId;
    private String sender;
    private String recipient;
    private float value;
    private String signature;

    private List<UTXO> inputs = new ArrayList<>();
    private List<UTXO> outputs = new ArrayList<>();

    private static final String GENESIS_ID = "0"; // id of genesis transaction

    public Transaction(String transactionId, String sender, String recipient, float value, String signature) {
        this.transactionId = transactionId;
        this.sender = sender;
        this.recipient = recipient;
        this.value = value;
        this.signature = signature;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public boolean isGenesis() {
        return transactionId.equals(GENESIS_ID);
    }

    public boolean verifySignature() {
        return CryptoUtil.verifyEcdsaSign(
            sender,
            sender + recipient + Float.toString(value),
            signature
            );
    }

    public boolean doProcess() {
        return isGenesis()
            ? processGenesisTransaction()
            : processTransaction();
    }

    private boolean processTransaction() {
        var senderUTXOs = SBChain.uTXOPool
            .select(v -> v.belongsTo(sender));

        // set inputs
        inputs = senderUTXOs.ceilingList(value);
        var inputsValue = inputs.stream()
            .map(UTXO::getValue)
            .reduce(0f, Float::sum);

        // validate transaction
        if (!verifySignature()) {
            System.out.println("# Transaction Signature failed to verify. Transaction discarded.");
            return false;
        }
        if (inputsValue < value) {
            System.out.println("# Not enough balance to send transaction. Transaction discarded.");
            return false;
        }
        if (inputsValue < SBChain.minimumTransactionValue) {
            System.out.println("# Transaction Inputs too small: " + inputsValue + ". Transaction discarded.");
            return false;

        }

        // set outputs in transaction
        outputs = List.of(
            new UTXO(recipient, value, transactionId),
            new UTXO(sender, inputsValue - value, transactionId));

        // update UTXOPool of blockchain
        SBChain.uTXOPool.putAll(outputs);
        SBChain.uTXOPool.removeAll(inputs);

        return true;
    }

    private boolean processGenesisTransaction() {
        // set outputs in transaction
        outputs = List.of(new UTXO(recipient, value, transactionId));

        // update UTXOPool of blockchain
        SBChain.uTXOPool.putAll(outputs);

        return true;
    }
}
