package com.myexample.wallet;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.myexample.blockchain.SBChain;
import com.myexample.transaction.Transaction;
import com.myexample.transaction.UTXOPool;
import com.myexample.utils.CryptoUtil;

public class Wallet {

    private KeyPair keyPair;
    private UTXOPool walletUTXOPool = new UTXOPool();

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

    public void updateUTXOPool() {
        SBChain.uTXOPool.values().stream()
            .filter(uTXO -> uTXO.belongsTo(getPublicKey()))
            .sorted((o1, o2) -> Float.compare(o1.getValue(), o2.getValue())) //TODO: ordering
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
