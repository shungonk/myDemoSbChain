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

    public float getBalance() {
        return walletUTXOPool.totalValue();
    }

    public void updateuTXOPool() {
        SBChain.uTXOPool.values().stream()
            .filter(uTXO -> uTXO.belongsTo(getPublicKey()))
            .forEach(walletUTXOPool::put);
    }

    public Transaction sendFunds(PublicKey recipient, float value) {
        updateuTXOPool();
        
        if (getBalance() < value) {
            System.out.println("#Not enough funds to send transaction. Transaction discarded.");
            return null;
        }

        var inputs = walletUTXOPool.ceilingList(value);
        var newTransaction = Transaction.createInstance(keyPair, recipient, value, inputs);
		walletUTXOPool.removeAll(inputs);

		return newTransaction;
    }
}
