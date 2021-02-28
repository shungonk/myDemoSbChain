package com.myexample.blockchain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.time.Instant;

import com.google.gson.GsonBuilder;
import com.myexample.utils.LogWriter;
import com.myexample.utils.SecurityUtil;
import com.myexample.utils.StringUtil;

public class Transaction implements Serializable {

    private static final long serialVersionUID = -4830434376199056194L;

    private String transactionId;
    private long timestamp;
    private String senderAddress;
    private String recipientAddress;
    private BigDecimal amount;
    private String signature;

    public Transaction(String senderAddress, String recipientAddress, BigDecimal amount, String signature) {
        this.timestamp = Instant.now().toEpochMilli();
        this.senderAddress = senderAddress;
        this.recipientAddress = recipientAddress;
        this.amount = amount.setScale(SBChain.TRANSACTION_AMOUNT_SCALE);
        this.signature = signature;
        this.transactionId = calculateHash();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public long getTimestamp() {
        return timestamp;
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

    public String getSignature() {
        return signature;
    }

    public String calculateHash() {
        try {
            return SecurityUtil.sha256(
                Long.toString(timestamp) + senderAddress + recipientAddress + amount.toPlainString() + signature);
        } catch (GeneralSecurityException e) {
            LogWriter.severe("Falied to apply hash Algorithm");
            throw new RuntimeException(e);
        }
    }
    
    public String marshalJson() {
        return StringUtil.toJson(this);
    }

    public String marshalJsonPrettyPrinting() {
        var gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        return gsonBuilder.toJson(this);
    }
}
