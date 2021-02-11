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

    public KeyPair getKeyPair() {
        return keyPair;
    }
    
    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }
}
