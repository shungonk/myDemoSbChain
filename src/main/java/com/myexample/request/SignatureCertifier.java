package com.myexample.request;

import java.security.GeneralSecurityException;

import com.google.gson.GsonBuilder;
import com.myexample.utils.LogWriter;
import com.myexample.utils.SecurityUtil;
import com.myexample.utils.StringUtil;

public abstract class SignatureCertifier {

    protected String publicKey; // will be set in signate method
    protected String signature; // will be set in signate method

    protected SignatureCertifier() {}

    public String getPublicKey() {
        return publicKey;
    }

    public String getSignature() {
        return signature;
    }

    public abstract byte[] getData();

    public abstract boolean validateFields();

    public final void signate(String privateKey, String publicKey) {
        try {
            this.publicKey = publicKey;
            this.signature = SecurityUtil.createEcdsaSign(privateKey, getData());
        } catch (GeneralSecurityException e) {
            LogWriter.warning("Failed to create ECDSA signature");
        }
    }

    public final boolean verifySignature() {
        try {
            return SecurityUtil.verifyEcdsaSign(publicKey, getData(), signature);
        } catch (GeneralSecurityException e) {
            LogWriter.warning("Failed to verity ECDSA signature");
            return false;
        }
    }

    public String toJson() {
        return StringUtil.toJson(this);
    }

    public String toJsonPrettyPrinting() {
        var gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        return gsonBuilder.toJson(this);
    }
}