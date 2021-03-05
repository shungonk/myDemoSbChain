package com.myexample.request;

import java.math.BigDecimal;
import java.security.GeneralSecurityException;

import com.myexample.common.LogWriter;
import com.myexample.common.SecurityUtil;

public class PurchaseRequest extends SignatureCertifier {

    private String address;
    private BigDecimal amount;
    private long timestamp;

    public PurchaseRequest(String address, BigDecimal amount, long timestamp) {
        this.address = address;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getAddress() {
        return address;
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
        String data = address + amount.toPlainString() + Long.toString(timestamp);
        return data.getBytes();
    }

    @Override
    public boolean validateFields() {
        if (publicKey == null || publicKey.isBlank() ||
            address == null || address.isBlank() ||
            amount == null ||
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
            return SecurityUtil.verifyAddressByPublicKey(address, publicKey);
        } catch (GeneralSecurityException e) {
            LogWriter.warning("Falied to verify address by public key");
            return false;
        }
    }
}
