package com.myexample.request;

import java.math.BigDecimal;
import java.time.Instant;

import com.google.gson.GsonBuilder;
import com.myexample.utils.SecurityUtil;
import com.myexample.utils.StringUtil;

public class PurchaseRequest {

    private String publicKey; // recipient
    private String address;   // recipient
    private BigDecimal value;
    private String signature;
    private long timestamp;

    public PurchaseRequest(String publicKey, String address, BigDecimal value, String signature) {
        this.publicKey = publicKey;
        this.address = address;
        this.value = value;
        this.signature = signature;
        this.timestamp = Instant.now().toEpochMilli();
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getAddress() {
        return address;
    }

    public BigDecimal getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean validatePurchaseRequest() {
        if (publicKey == null || publicKey.isBlank() ||
            address == null || address.isBlank() ||
            value == null || value.equals(BigDecimal.ZERO) ||
            signature == null || signature.isBlank() ||
            timestamp == 0) {
            return false;
        }
        return true;
    }

    public boolean validateValue() {
        return value.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean verifySignature() {
        return SecurityUtil.verifyEcdsaSign(
            publicKey,
            address + value.toPlainString() + Long.toString(timestamp),
            signature
            );
    }

    public boolean veritfyAddress() {
        return SecurityUtil.validateAddressByPublicKey(address, publicKey);
    }

    public String marshalJson() {
        return StringUtil.toJson(this);
    }

    public String marshalJsonPrettyPrinting() {
        var gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        return gsonBuilder.toJson(this);
    }

    public static PurchaseRequest fromJson(String json) {
        return StringUtil.fromJson(json, PurchaseRequest.class);
    }
    
}
