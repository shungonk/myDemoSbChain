package com.myexample.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.myexample.common.utils.CryptoUtil;

public class TransactionRequest {

    private String senderPublicKey;
    private String senderAddress;
    private String recipientAddress;
    private float value;
    private String signature;

    public TransactionRequest(String senderPublicKey, String senderAddress, String recipientAddress, float value, String signature) {
        this.senderPublicKey = senderPublicKey;
        this.senderAddress = senderAddress;
        this.recipientAddress = recipientAddress;
        this.value = value;
        this.signature = signature;
    }

    public String getSenderPublicKey() {
        return senderPublicKey;
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

    public String getSignature() {
        return signature;
    }

    public String calculateHash() {
        return CryptoUtil.sha256(senderAddress + recipientAddress + Float.toString(value));
    }

    public boolean verifySignature() {
        return CryptoUtil.verifyEcdsaSign(
            senderPublicKey,
            senderAddress + recipientAddress + Float.toString(value),
            signature
            );
    }

    public String marshalJson() {
        return new Gson().toJson(this);
    }

    public String marshalJsonPrettyPrinting() {
        var gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        return gsonBuilder.toJson(this);
    }

    public static TransactionRequest fromJson(String json) {
        return new Gson().fromJson(json, TransactionRequest.class);
    }
    
}
