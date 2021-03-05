package com.myexample.request;

import java.math.BigDecimal;
import java.security.GeneralSecurityException;

import com.myexample.common.LogWriter;
import com.myexample.common.SecurityUtil;

public class TransactionRequest extends SignatureCertifier {

    private String senderAddress;
    private String recipientAddress;
    private BigDecimal amount;
    private long timestamp;

    public TransactionRequest(String senderAddress, String recipientAddress, BigDecimal amount, long timestamp) {
        this.senderAddress = senderAddress;
        this.recipientAddress = recipientAddress;
        this.amount = amount;
        this.timestamp = timestamp;
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

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public byte[] getData() {
        // signature will be unique because data contain timestamp
        String data = senderAddress + recipientAddress + amount.toPlainString() + Long.toString(timestamp);
        return data.getBytes();
    }

    @Override
    public boolean validateFields() {
        if (publicKey == null || publicKey.isBlank() ||
            senderAddress == null || senderAddress.isBlank() ||
            recipientAddress == null || recipientAddress.isBlank() ||
            amount == null || amount.equals(BigDecimal.ZERO) ||
            signature == null || signature.isBlank() ||
            timestamp == 0) {
            return false;
        }
        return true;
    }

    public boolean validateAmount() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public final boolean verifyAddress() {
        try {
            return SecurityUtil.verifyAddressByPublicKey(senderAddress, publicKey);
        } catch (GeneralSecurityException e) {
            LogWriter.warning("Falied to verify address by public key");
            return false;
        }
    }

    public boolean isRecipientAddressBase58() {
        var pattern = "[123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz]{34}";
		return recipientAddress.matches(pattern);
    }
}
