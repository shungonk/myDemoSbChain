package com.myexample.wallet;

import java.security.Security;

import com.myexample.common.TransactionRequest;
import com.myexample.common.utils.CryptoUtil;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class WalletServer {
    public static void main( String[] args ) {
		Security.addProvider(new BouncyCastleProvider());

		var walletA = new Wallet();
		var walletB = new Wallet();
		var coinbase = new Wallet();

		System.out.println("Transaction base -> A");
		var tranForm1 = new TransactionForm(
			coinbase.getPrivateKey(),
			coinbase.getPublicKey(), 
			coinbase.getAddress(),
			walletA.getAddress(),
			100);
		sendTransaction(tranForm1);

		System.out.println("Transaction A -> B");
		var tranForm2 = new TransactionForm(
			walletA.getPrivateKey(),
			walletA.getPublicKey(), 
			walletA.getAddress(),
			walletB.getAddress(),
			80);
		sendTransaction(tranForm2);

		System.out.println("Transaction B -> A");
		var tranForm3 = new TransactionForm(
			walletB.getPrivateKey(),
			walletB.getPublicKey(), 
			walletB.getAddress(),
			walletA.getAddress(),
			45);
		sendTransaction(tranForm3);

		System.out.println("Transaction B -> A");
		var tranForm4 = new TransactionForm(
			walletB.getPrivateKey(),
			walletB.getPublicKey(), 
			walletB.getAddress(),
			walletA.getAddress(),
			90);
		sendTransaction(tranForm4);

    }

	public static void sendTransaction(TransactionForm form) {

		// create request to blockchain server
		var request = new TransactionRequest(
			form.getSenderPublicKey(), 
			form.getSenderAddress(),
			form.getRecipientAddress(),
			form.getValue(),
			form.generateSignature());
		var requestJson = request.marshalJson();

		// debug
		System.out.println("======= Transaction =======");
		// System.out.println(request.marshalJsonPrettyPrinting());
		System.out.println(request.marshalJson());
		System.out.println("Is signature valid?: " + request.verifySignature());

		// request to blockchain server and get response XXX
		var response = "XXX";

		// send responst to client
	}

	public static void test() {
		var senderPublicKey = "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEM0mioVbXjoZrtQNWsIUhpGZY08wHdNWXqAIxPkQR8gCRl4nsWrQ4YJd6TwFW1vrafYQ7KqDqaEvVvdNHLQAWfw\u003d\u003d";
		var senderAddress = "1MgsoN669KWhdTqX253aJsahqmr8ZjkWR1";
		var recipirntAddress = "14S5tUuBJkoi1mkPx6XZUZUUsKhzSBqrK5";
		var value = 45.0f;
		var signature = "MEUCIQDr6AfYrTy25bEWcBh0bhItleQI0SdIxEv08QnS0VoX1gIgYhoIItKkwBPJV8aHTkazjjnWXOPmDmtiP4d+LgrhsAA\u003d";

		boolean isValid = CryptoUtil.verifyEcdsaSign(
			senderPublicKey,
			senderAddress + recipirntAddress + Float.toString(value),
			signature);

		System.out.println(isValid);
	}
}
