package com.myexample.request;

import java.math.BigDecimal;

import com.google.gson.GsonBuilder;
import com.myexample.common.utils.SecurityUtil;
import com.myexample.common.utils.StringUtil;

public class TransactionRequest {

    private String senderPublicKey;
    private String senderAddress;
    private String recipientAddress;
    private BigDecimal value;
    private String signature;

    public TransactionRequest(String senderPublicKey, String senderAddress, String recipientAddress, BigDecimal value, String signature) {
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

    public BigDecimal getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }

    public boolean validateTransactionRequest() {
        if (senderPublicKey == null || senderPublicKey.isBlank() ||
            senderAddress == null || senderAddress.isBlank() ||
            recipientAddress == null || recipientAddress.isBlank() ||
            value == null || value.equals(BigDecimal.ZERO) ||
            signature == null || signature.isBlank()) {
            return false;
        }
        return true;
    }

    public boolean validateValue() {
        return value.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean verifySignature() {
        return SecurityUtil.verifyEcdsaSign(
            senderPublicKey,
            senderAddress + recipientAddress + value.toPlainString(),
            signature
            );
    }

    public boolean veritfyAddress() {
        return SecurityUtil.validateAddressByPublicKey(senderAddress, senderPublicKey);
    }

    public String marshalJson() {
        return StringUtil.toJson(this);
    }

    public String marshalJsonPrettyPrinting() {
        var gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        return gsonBuilder.toJson(this);
    }

    public static TransactionRequest fromJson(String json) {
        return StringUtil.fromJson(json, TransactionRequest.class);
    }
    
}
