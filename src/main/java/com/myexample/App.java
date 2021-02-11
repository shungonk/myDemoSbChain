package com.myexample;

import java.security.Security;

import com.myexample.transaction.Transaction;
import com.myexample.utils.CryptoUtil;
import com.myexample.wallet.Wallet;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class App {
    public static void main( String[] args ) {
		// Block genesisBlock = new Block("Hi im the first block", "0");
		// System.out.println("Hash for block 1 : " + genesisBlock.getHash());
		
		// Block secondBlock = new Block("Yo im the second block",genesisBlock.getHash());
		// System.out.println("Hash for block 2 : " + secondBlock.getHash());
		
		// Block thirdBlock = new Block("Hey im the third block",secondBlock.getHash());
		// System.out.println("Hash for block 3 : " + thirdBlock.getHash());

		//Setup Bouncey castle as a Security Provider
		Security.addProvider(new BouncyCastleProvider()); 
		//Create the new wallets
		Wallet walletA = new Wallet();
		Wallet walletB = new Wallet();
		//Test public and private keys
		System.out.println("Private and public keys:");
		System.out.println(CryptoUtil.encodeKey(walletA.getPrivateKey()));
		System.out.println(CryptoUtil.encodeKey(walletA.getPublicKey()));
		//Create a test transaction from WalletA to walletB 
		Transaction transaction = Transaction.buildInstance(walletA.getKeyPair(), walletB.getPublicKey(), 5, null);
		//Verify the signature works and verify it from the public key
		System.out.println("Is signature verified");
		System.out.println(transaction.verifySignature());
    }
}
