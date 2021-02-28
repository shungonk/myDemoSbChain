package com.myexample.client;

import com.myexample.utils.SecurityUtil;

public class Wallet {

    private String privateKey;
    private String publicKey;
    private String address;

    public Wallet() {
        try {
            var keyPair = SecurityUtil.generateKeyPair();            
            var pvt = keyPair.getPrivate();
            var pub = keyPair.getPublic();

            this.privateKey = SecurityUtil.encodeKeyToString(pvt);
            this.publicKey = SecurityUtil.encodeKeyToString(pub);
            this.address = SecurityUtil.getAddressFromPublicKey(pub);
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getAddress() {
        return address;
    }

}
