package com.myexample.request;

import java.security.GeneralSecurityException;

import com.myexample.utils.LogWriter;
import com.myexample.utils.SecurityUtil;

public class MiningRequest extends SignatureCertifier {

    private String address;
    private long timestamp;

    public MiningRequest(String address, long timestamp) {
        this.address = address;
        this.timestamp = timestamp;
    }

    public String getAddress() {
        return address;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public byte[] getData() {
        // signature will be unique because data contain timestamp
        String data = address + Long.toString(timestamp);
        return data.getBytes();
    }

    @Override
    public boolean validateFields() {
        if (publicKey == null || publicKey.isBlank() ||
            address == null || address.isBlank() ||
            signature == null || signature.isBlank() ||
            timestamp == 0) {
            return false;
        }
        return true;
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
