package com.myexample.request;

import java.math.BigDecimal;

import com.myexample.utils.SecurityUtil;

public class PurchaseRequest extends SignatureRequest {

    private String address;
    private BigDecimal value;
    private long timestamp;

    public PurchaseRequest(String address, BigDecimal value, long timestamp) {
        this.address = address;
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getAddress() {
        return address;
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
        String data = address + value.toPlainString() + Long.toString(timestamp);
        return data.getBytes();
    }

    @Override
    public boolean validateFields() {
        if (publicKey == null || publicKey.isBlank() ||
            address == null || address.isBlank() ||
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
        return SecurityUtil.validateAddressByPublicKey(address, publicKey);
    }
}
