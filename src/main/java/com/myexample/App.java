package com.myexample;

import java.security.Security;

import com.myexample.blockchain.SBChain;
import com.myexample.wallet.Wallet;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class App {
    public static void main( String[] args ) {
        Security.addProvider(new BouncyCastleProvider());

		var walletA = new Wallet();
		var walletB = new Wallet();
		var coinbase = new Wallet();


		// System.out.println("======================================");
		// String pubAStr = CryptoUtil.encodeKey(walletA.getPublicKey());
		// PublicKey pubAKey = CryptoUtil.decodePublicKey(pubAStr);
		// String pubStrRe = CryptoUtil.encodeKey(pubAKey);
		// System.out.println(pubAStr);
		// System.out.println(pubStrRe);

		// System.out.println("======================================");
		// String priAStr = CryptoUtil.encodeKey(walletA.getPrivateKey());
		// PrivateKey priAKey = CryptoUtil.decodePrivateKey(priAStr);
		// String priAStrRe = CryptoUtil.encodeKey(priAKey);
		// System.out.println(priAStr);
		// System.out.println(priAStrRe);

		System.out.println("======================================");
		SBChain.addTransaction(coinbase.sendGenesisFunds(walletA.getPublicKey(), 100));
		System.out.println("WalletA's balance: " + walletA.getBalance());
		System.out.println("WalletB's balance: " + walletB.getBalance());
		SBChain.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 50));
		System.out.println("WalletA's balance: " + walletA.getBalance());
		System.out.println("WalletB's balance: " + walletB.getBalance());
		SBChain.mining();

		System.out.println("======================================");
		SBChain.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 10));
		System.out.println("WalletA's balance: " + walletA.getBalance());
		System.out.println("WalletB's balance: " + walletB.getBalance());
		SBChain.addTransaction(walletB.sendFunds(walletA.getPublicKey(), 100));
		System.out.println("WalletA's balance: " + walletA.getBalance());
		System.out.println("WalletB's balance: " + walletB.getBalance());
		SBChain.addTransaction(walletB.sendFunds(walletA.getPublicKey(), 36));
		System.out.println("WalletA's balance: " + walletA.getBalance());
		System.out.println("WalletB's balance: " + walletB.getBalance());
		SBChain.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 0.001f));
		System.out.println("WalletA's balance: " + walletA.getBalance());
		System.out.println("WalletB's balance: " + walletB.getBalance());
		SBChain.mining();
		
		System.out.println("======================================");
		System.out.println(SBChain.isChainValid());
		System.out.println(SBChain.marshalJson());

    }
}
