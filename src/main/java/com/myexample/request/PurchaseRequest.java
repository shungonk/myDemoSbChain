package com.myexample.request;

import com.google.gson.GsonBuilder;
import com.myexample.common.utils.SecurityUtil;
import com.myexample.common.utils.StringUtil;

public class PurchaseRequest {

    private String publicKey;
    private String address;
    private float value;
    private String signature;

    public PurchaseRequest(String publicKey, String address, float value, String signature) {
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

    public float getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }

    public boolean validatePurchaseRequest() {
        if (publicKey == null || publicKey.isBlank() ||
            address == null || address.isBlank() ||
            signature == null || signature.isBlank()) {
            return false;
        }
        return true;
    }

    public boolean validateValue() {
        return Float.compare(value, 0f) > 0;
    }

    public boolean verifySignature() {
        return SecurityUtil.verifyEcdsaSign(
            publicKey,
            address + Float.toString(value),
            signature
            );
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
