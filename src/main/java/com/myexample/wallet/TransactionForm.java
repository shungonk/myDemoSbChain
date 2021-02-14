package com.myexample.wallet;

import com.myexample.utils.CryptoUtil;

public class TransactionForm {

    private String senderPrivateKey;
    private String senderPublicKey;
    private String recipientPublicKey;
    private float value;

    public TransactionForm(String senderPrivateKey, String senderPublicKey, String recipientPublicKey, float value) {
        this.senderPrivateKey = senderPrivateKey;
        this.senderPublicKey = senderPublicKey;
        this.recipientPublicKey = recipientPublicKey;
        this.value = value;
    }

    public String getSenderPrivateKey() {
        return senderPrivateKey;
    }

    public String getSenderPublicKey() {
        return senderPublicKey;
    }

    public String getRecipientPublicKey() {
        return recipientPublicKey;
    }

    public float getValue() {
        return value;
    }

    public String calculateHash() {
        return CryptoUtil.sha256(senderPublicKey + recipientPublicKey + Float.toString(value));
    }

    public String generateSignature() {
        return CryptoUtil.createEcdsaSign(
            senderPrivateKey,
            senderPublicKey + recipientPublicKey + Float.toString(value)
            );
    }
}
