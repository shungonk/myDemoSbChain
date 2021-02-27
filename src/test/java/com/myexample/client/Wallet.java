package com.myexample.client;

import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;

import com.myexample.utils.SecurityUtil;

public class Wallet {

    private String privateKey;
    private String publicKey;
    private String address;

    public Wallet() {
        try {
            // create the KeyPair, from which you can obtain the public and private keys.
            var keyGenerator = KeyPairGenerator.getInstance("EC"); 
            var ecGenSpec = new ECGenParameterSpec("secp256k1");
            keyGenerator.initialize(ecGenSpec);
            var keyPair = keyGenerator.genKeyPair();
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
