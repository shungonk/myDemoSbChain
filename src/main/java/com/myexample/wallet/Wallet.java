package com.myexample.wallet;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.myexample.utils.CryptoUtil;

public class Wallet {

    private KeyPair keyPair;

    public Wallet() {
        this.keyPair = CryptoUtil.generateKeyPair();
    }

    public PrivateKey privateKey() {
        return keyPair.getPrivate();
    }
    
    public PublicKey publicKey() {
        return keyPair.getPublic();
    }

    public String privateKeyString() {
        return CryptoUtil.encodeKeyToString(keyPair.getPrivate());
    }

    public String publicKeyString() {
        return CryptoUtil.encodeKeyToString(keyPair.getPublic());
    }
}
