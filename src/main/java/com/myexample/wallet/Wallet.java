package com.myexample.wallet;

import java.security.KeyPair;

import com.myexample.utils.CryptoUtil;

public class Wallet {

    private KeyPair keyPair;

    public Wallet() {
        this.keyPair = CryptoUtil.generateKeyPair();
    }

    public String privateKeyString() {
        return CryptoUtil.encodeKeyToString(keyPair.getPrivate());
    }

    public String publicKeyString() {
        return CryptoUtil.encodeKeyToString(keyPair.getPublic());
    }
}
