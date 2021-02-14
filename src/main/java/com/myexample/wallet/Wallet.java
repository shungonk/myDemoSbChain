package com.myexample.wallet;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;

import com.myexample.blockchain.SBChain;
import com.myexample.transaction.Transaction;
import com.myexample.transaction.UTXOPool;

public class Wallet {

    private KeyPair keyPair;
    private UTXOPool walletUTXOPool = new UTXOPool();

    public Wallet() {
        try {
            var generator = KeyPairGenerator.getInstance("ECDSA", "BC");
            var random = SecureRandom.getInstance("SHA1PRNG");
            var eSpec = new ECGenParameterSpec("prime192v1");
            generator.initialize(eSpec, random);
            this.keyPair =  generator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException();
        }
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

    public void updateUTXOPool() {
        SBChain.uTXOPool.values().stream()
            .filter(uTXO -> uTXO.belongsTo(getPublicKey()))
            .forEach(walletUTXOPool::put);
    }

    public float getBalance() {
        updateUTXOPool();
        return walletUTXOPool.totalValue();
    }

    public Transaction sendFunds(PublicKey recipient, float value) {
        if (getBalance() < value) {
            System.out.println("#Not enough funds to send transaction. Transaction discarded.");
            return null;
        }

        var inputs = walletUTXOPool.ceilingList(value);
        var newTransaction = Transaction.create(keyPair, recipient, value, inputs);
		walletUTXOPool.removeAll(inputs);

		return newTransaction;
    }

    public Transaction sendGenesisFunds(PublicKey recipient, float value) {
		return Transaction.createGenesis(getKeyPair(), recipient, value);
    }
}
