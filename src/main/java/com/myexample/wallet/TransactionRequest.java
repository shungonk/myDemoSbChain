package com.myexample.wallet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TransactionRequest {

    private String senderPublicKey;
    private String recipientPublicKey;
    private float value;
    private String signature;
    private String transactionId;

    public TransactionRequest(String transactionId, String senderPublicKey, String recipientPublicKey, float value,
            String signature) {
        this.transactionId = transactionId;
        this.senderPublicKey = senderPublicKey;
        this.recipientPublicKey = recipientPublicKey;
        this.value = value;
        this.signature = signature;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getSender() {
        return senderPublicKey;
    }

    public String getRecipient() {
        return recipientPublicKey;
    }

    public float getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }

    public String marshalJson() {
        return new Gson().toJson(this);
    }

    public String marshalJsonPrettyPrinting() {
        var gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        return gsonBuilder.toJson(this);
    }
}
