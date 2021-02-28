package com.myexample.client;

import java.math.BigDecimal;

public class TransactionForm {

    private String senderPrivateKey;
    private String senderPublicKey;
    private String senderAddress;
    private String recipientAddress;
    private BigDecimal amount;

    public TransactionForm(String senderPrivateKey, String senderPublicKey, String senderAddress,
            String recipientAddress, BigDecimal amount) {
        this.senderPrivateKey = senderPrivateKey;
        this.senderPublicKey = senderPublicKey;
        this.senderAddress = senderAddress;
        this.recipientAddress = recipientAddress;
        this.amount = amount;
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

    public BigDecimal getAmount() {
        return amount;
    }
}
