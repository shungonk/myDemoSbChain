package com.myexample.blockchain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

import com.google.gson.GsonBuilder;
import com.myexample.utils.SecurityUtil;
import com.myexample.utils.StringUtil;

public class Transaction implements Serializable {

    private static final long serialVersionUID = -4830434376199056194L;

    private String transactionId;
    private long timestamp;
    private String senderAddress;
    private String recipientAddress;
    private BigDecimal value;

    public Transaction(String senderAddress, String recipientAddress, BigDecimal value) {
        this.timestamp = Instant.now().toEpochMilli();
        this.senderAddress = senderAddress;
        this.recipientAddress = recipientAddress;
        this.value = value.setScale(SBChain.VALUE_SCALE);
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

    public BigDecimal getValue() {
        return value;
    }

    public String calculateHash() {
        return SecurityUtil.sha256(
            Long.toString(timestamp) + senderAddress + recipientAddress + value.toPlainString()
            );
    }
    
    public String marshalJson() {
        return StringUtil.toJson(this);
    }

    public String marshalJsonPrettyPrinting() {
        var gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        return gsonBuilder.toJson(this);
    }
}
