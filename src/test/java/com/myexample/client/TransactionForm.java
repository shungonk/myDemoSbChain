package com.myexample.client;

import java.math.BigDecimal;

import com.myexample.utils.SecurityUtil;

public class TransactionForm {

    private String senderPrivateKey;
    private String senderPublicKey;
    private String senderAddress;
    private String recipientAddress;
    private BigDecimal value;

    public TransactionForm(String senderPrivateKey, String senderPublicKey, String senderAddress,
            String recipientAddress, BigDecimal value) {
        this.senderPrivateKey = senderPrivateKey;
        this.senderPublicKey = senderPublicKey;
        this.senderAddress = senderAddress;
        this.recipientAddress = recipientAddress;
        this.value = value;
    }

    public String getSenderPrivateKey() {
        return senderPrivateKey;
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

    public String generateSignature() {
        return SecurityUtil.createEcdsaSign(
            senderPrivateKey,
            senderAddress + recipientAddress + value.toPlainString()
            );
    }
    
}
