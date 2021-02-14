package com.myexample.wallet;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class WalletServer {
    public static void main( String[] args ) {
        Security.addProvider(new BouncyCastleProvider());

		var walletA = new Wallet();
		var walletB = new Wallet();
		var coinbase = new Wallet();
		var privateKeyStrBase = coinbase.privateKeyString();
		var publicKeyStrBase = coinbase.publicKeyString();
		var privateKeyStrA = walletA.privateKeyString();
		var publicKeyStrA = walletA.publicKeyString();
		var privateKeyStrB = walletB.privateKeyString();
		var publicKeyStrB = walletB.publicKeyString();

		System.out.println("Transaction base -> A");
		var tranForm1 = new TransactionForm(
			privateKeyStrBase, publicKeyStrBase, publicKeyStrA, 100);
		sendTransaction(tranForm1);

		System.out.println("Transaction A -> B");
		var tranForm2 = new TransactionForm(
			privateKeyStrA, publicKeyStrA, publicKeyStrB, 80);
		sendTransaction(tranForm2);

		System.out.println("Transaction B -> A");
		var tranForm3 = new TransactionForm(
			privateKeyStrB, publicKeyStrB, publicKeyStrA, 45);
		sendTransaction(tranForm3);

		System.out.println("Transaction B -> A");
		var tranForm4 = new TransactionForm(
			privateKeyStrB, publicKeyStrB, publicKeyStrA, 90);
		sendTransaction(tranForm4);

    }

	public static void sendTransaction(TransactionForm form) {

		// create request to blockchain server
		var request = new TransactionRequest(
			form.calculateHash(),
			form.getSenderPublicKey(), 
			form.getRecipientPublicKey(), 
			form.getValue(), 
			form.generateSignature());
		var requestJson = request.marshalJson();

		// debug
		System.out.println("======= Transaction =======");
		// System.out.println(request.marshalJsonPrettyPrinting());
		System.out.println(request.marshalJson());

		// request to blockchain server and get response XXX
		var response = "XXX";

		// send responst to client
	}
}
