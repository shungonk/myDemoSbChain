package com.myexample;

import java.security.Security;

import com.myexample.blockchain.SBChain;
import com.myexample.transaction.Transaction;
import com.myexample.utils.CryptoUtil;
import com.myexample.wallet.Wallet;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class App {
    public static void main( String[] args ) {
        SBChain.addNewBlock("Yo im the second block");
        SBChain.addNewBlock("Hey im the third block");
        SBChain.addNewBlock("Hoo im the fourth block");
			
		System.out.println(SBChain.marshalJson());

		//Setup Bouncey castle as a Security Provider
		Security.addProvider(new BouncyCastleProvider()); 
		//Create the new wallets
		var walletA = new Wallet();
		var walletB = new Wallet();
		//Test public and private keys
		System.out.println("Private and public keys:");
		System.out.println(CryptoUtil.encodeKey(walletA.getPrivateKey()));
		System.out.println(CryptoUtil.encodeKey(walletA.getPublicKey()));
		//Create a test transaction from WalletA to walletB 
		var transaction = Transaction.createInstance(walletA.getKeyPair(), walletB.getPublicKey(), 5, null);
		//Verify the signature works and verify it from the public key
		System.out.println("Is signature verified");
		System.out.println(transaction.verifySignature());
    }
}
