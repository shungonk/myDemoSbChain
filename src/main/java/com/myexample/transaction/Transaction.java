package com.myexample.transaction;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import com.myexample.blockchain.SBChain;
import com.myexample.utils.CryptoUtil;

public class Transaction {

    private String transactionId; // this is also the hash of the transaction.
    private PublicKey sender;     // senders address/public key.
    private PublicKey recipient;  // Recipients address/public key.
    private float value;
    private byte[] signature;     // this is to prevent anybody else from spending funds in our wallet.

    private List<UTXO> inputs = new ArrayList<>();
    private List<UTXO> outputs = new ArrayList<>();

    private static int sequence = 0; // a rough count of how many transactions have been generated.
    private static final String GENESIS_ID = "0"; // id of genesis transaction

    private Transaction(KeyPair senderKeyPair, PublicKey recipient, float value) {
        this.sender = senderKeyPair.getPublic();
        this.recipient = recipient;
        this.value = value;
        this.generateSignature(senderKeyPair.getPrivate());
    }

    public static Transaction create(KeyPair senderKeyPair, PublicKey recipient, float value, List<UTXO> inputs) {
        var transaction = new Transaction(senderKeyPair, recipient, value);
        transaction.inputs = inputs;
        transaction.calculateHashedId();
        sequence++;
        return transaction;
    }

    public static Transaction createGenesis(KeyPair senderKeyPair, PublicKey recipient, float value) {
        var transaction = new Transaction(senderKeyPair, recipient, value);
        transaction.transactionId = GENESIS_ID;
        sequence++;
        return transaction;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public PublicKey getSender() {
        return sender;
    }

    public PublicKey getRecipient() {
        return recipient;
    }

    public float getValue() {
        return value;
    }

    public byte[] getSignature() {
        return signature;
    }

    public boolean isGenesis() {
        return transactionId.equals(GENESIS_ID);
    }

    private void calculateHashedId() {
        transactionId = CryptoUtil.sha256(CryptoUtil.encodeKey(sender) + CryptoUtil.encodeKey(recipient) +
            Float.toString(value) + Integer.toString(sequence));
    }

    private void generateSignature(PrivateKey privateKey) {
        var data = CryptoUtil.encodeKey(sender) + CryptoUtil.encodeKey(recipient) + Float.toString(value);
        signature = CryptoUtil.ecdsaSign(privateKey, data);
    }

    public boolean verifySignature() {
        var data = CryptoUtil.encodeKey(sender) + CryptoUtil.encodeKey(recipient) + Float.toString(value);
        return CryptoUtil.verifyEcdsaSign(sender, data, signature);
    }

    public float getInputsValue() {
        return inputs.stream()
            .map(UTXO::getValue)
            .reduce(0f, Float::sum);
    }

    public float getOutputsValue() {
        return outputs.stream()
            .map(UTXO::getValue)
            .reduce(0f, Float::sum);
    }

    public boolean doProcess() {
        return isGenesis()
            ? processGenesisTransaction()
            : processTransaction();
    }

    public boolean processTransaction() {
        if (!verifySignature()) {
            System.out.println("#Transaction Signature failed to verify.");
            return false;
        }
        
        if (getInputsValue() < SBChain.minimumTransactionValue) {
            System.out.println("#Transaction Inputs too small: " + getInputsValue());
            return false;
        }

        // create outputs and set in transaction
        var leftOver = getInputsValue() - value;
        outputs.add(new UTXO(recipient, value, transactionId));
        outputs.add(new UTXO(sender, leftOver, transactionId));

        // update UTXOPool of blockchain
        SBChain.uTXOPool.putAll(outputs);
        SBChain.uTXOPool.removeAll(inputs);

        return true;
    }

    public boolean processGenesisTransaction() {
        if (!verifySignature()) {
            System.out.println("#Transaction Signature failed to verify.");
            return false;
        }

        // create outputs and set in transaction
        outputs.add(new UTXO(recipient, value, transactionId));

        // update UTXOPool of blockchain
        SBChain.uTXOPool.putAll(outputs);

        return true;
    }
}
