package com.myexample.transaction;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import com.myexample.utils.CryptoUtil;

public class Transaction {

    private String transactionId; // this is also the hash of the transaction.
    private PublicKey sender;     // senders address/public key.
    private PublicKey recipient;  // Recipients address/public key.
    private float value;
    private byte[] signature;     // this is to prevent anybody else from spending funds in our wallet.

    private List<TransactionInput> inputs = new ArrayList<>();
    private List<TransactionOutput> outputs = new ArrayList<>();

    private static int sequence = 0; // a rough count of how many transactions have been generated. 

    private Transaction() {}

    public static Transaction buildInstance(KeyPair senderKeyPair, PublicKey recipient, float value, List<TransactionInput> inputs) {
        Transaction transaction = new Transaction();
        transaction.sender = senderKeyPair.getPublic();
        transaction.recipient = recipient;
        transaction.value = value;
        transaction.inputs = inputs;
        transaction.calculateHash();
        transaction.generateSignature(senderKeyPair.getPrivate());
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

    public List<TransactionInput> getInputs() {
        return inputs;
    }

    public List<TransactionOutput> getOutputs() {
        return outputs;
    }

    private String calculateHash() {
        sequence++;
        return CryptoUtil.sha256(
            CryptoUtil.encodeKey(sender) + CryptoUtil.encodeKey(recipient) +
            Float.toString(value) + Integer.toString(sequence));
    }

    private void generateSignature(PrivateKey privateKey) {
        String data = CryptoUtil.encodeKey(sender) + CryptoUtil.encodeKey(recipient) + Float.toString(value);
        signature = CryptoUtil.ecdsaSign(privateKey, data);
    }

    public boolean verifySignature() {
        String data = CryptoUtil.encodeKey(sender) + CryptoUtil.encodeKey(recipient) + Float.toString(value);
        return CryptoUtil.verifyEcdsaSign(sender, data, signature);
    }
}
