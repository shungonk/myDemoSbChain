package com.myexample.request;

import com.google.gson.GsonBuilder;
import com.myexample.utils.SecurityUtil;
import com.myexample.utils.StringUtil;

public abstract class SignatureRequest {

    protected String publicKey; // will be set in signate method
    protected String signature; // will be set in signate method

    protected SignatureRequest() {}

    public String getPublicKey() {
        return publicKey;
    }

    public String getSignature() {
        return signature;
    }

    public abstract byte[] getData();

    public abstract boolean validateFields();

    public final void signate(String privateKey, String publicKey) {
        this.publicKey = publicKey;
        this.signature = SecurityUtil.createEcdsaSign(privateKey, getData());
    }

    public final boolean verifySignature() {
        return SecurityUtil.verifyEcdsaSign(publicKey, getData(), signature);
    }

    public String marshalJson() {
        return StringUtil.toJson(this);
    }

    public String marshalJsonPrettyPrinting() {
        var gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        return gsonBuilder.toJson(this);
    }
}