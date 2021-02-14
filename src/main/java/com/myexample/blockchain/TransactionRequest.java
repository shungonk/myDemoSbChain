package com.myexample.blockchain;

import com.google.gson.Gson;

public class TransactionRequest {

    private String transactionId;
    private String senderPublicKey;
    private String recipientPublicKey;
    private float value;
    private String signature;

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

    public static TransactionRequest fromJson(String json) {
        return new Gson().fromJson(json, TransactionRequest.class);
    }
}
