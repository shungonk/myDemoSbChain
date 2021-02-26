package com.myexample.request;

import java.math.BigDecimal;

import com.google.gson.GsonBuilder;
import com.myexample.common.utils.SecurityUtil;
import com.myexample.common.utils.StringUtil;

public class PurchaseRequest {

    private String publicKey; // recipient
    private String address;   // recipient
    private BigDecimal value;
    private String signature;

    public PurchaseRequest(String publicKey, String address, BigDecimal value, String signature) {
        this.publicKey = publicKey;
        this.address = address;
        this.value = value;
        this.signature = signature;
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

    public boolean validatePurchaseRequest() {
        if (publicKey == null || publicKey.isBlank() ||
            address == null || address.isBlank() ||
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
            publicKey,
            address + value.toPlainString(),
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
