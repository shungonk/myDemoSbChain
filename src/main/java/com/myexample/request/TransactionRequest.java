package com.myexample.request;

import java.math.BigDecimal;

import com.myexample.utils.SecurityUtil;

public class TransactionRequest extends SignatureRequest {

    private String senderAddress;
    private String recipientAddress;
    private BigDecimal value;
    private long timestamp;

    public TransactionRequest(String senderAddress, String recipientAddress, BigDecimal value, long timestamp) {
        this.senderAddress = senderAddress;
        this.recipientAddress = recipientAddress;
        this.value = value;
        this.timestamp = timestamp;
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

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public byte[] getData() {
        // signature will be unique because data contain timestamp
        String data = senderAddress + recipientAddress + value.toPlainString() + Long.toString(timestamp);
        return data.getBytes();
    }

    @Override
    public boolean validateFields() {
        if (publicKey == null || publicKey.isBlank() ||
            senderAddress == null || senderAddress.isBlank() ||
            recipientAddress == null || recipientAddress.isBlank() ||
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
    
    public final boolean verifyAddress() {
        return SecurityUtil.validateAddressByPublicKey(senderAddress, publicKey);
    }
}
