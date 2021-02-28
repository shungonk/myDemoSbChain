package com.myexample.client;

import java.math.BigDecimal;
import java.security.Security;
import java.time.Instant;

import com.myexample.request.TransactionRequest;
import com.myexample.utils.SecurityUtil;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class WalletServer {
    public static void main( String[] args ) {
		Security.addProvider(new BouncyCastleProvider());

		var walletA = new Wallet();
		var walletB = new Wallet();
		var coinbase = new Wallet();
		System.out.println("Wallet A");
		System.out.println(" Address: " + walletA.getAddress());
		System.out.println(" Private Key: " + walletA.getPrivateKey());
		System.out.println(" Public Key: " + walletA.getPublicKey());
		System.out.println("Wallet B");
		System.out.println(" Address: " + walletB.getAddress());
		System.out.println(" Private Key: " + walletB.getPrivateKey());
		System.out.println(" Public Key: " + walletB.getPublicKey());

		System.out.println("======== Transaction base -> A ========");
		var tranForm1 = new TransactionForm(
			coinbase.getPrivateKey(),
			coinbase.getPublicKey(), 
			coinbase.getAddress(),
			walletA.getAddress(),
			new BigDecimal(100));
		sendTransaction(tranForm1);

		System.out.println("======== Transaction A -> B ========");
		var tranForm2 = new TransactionForm(
			walletA.getPrivateKey(),
			walletA.getPublicKey(), 
			walletA.getAddress(),
			walletB.getAddress(),
			new BigDecimal(80));
		sendTransaction(tranForm2);

		System.out.println("======== Transaction B -> A ========");
		var tranForm3 = new TransactionForm(
			walletB.getPrivateKey(),
			walletB.getPublicKey(), 
			walletB.getAddress(),
			walletA.getAddress(),
			new BigDecimal(45));
		sendTransaction(tranForm3);

		System.out.println("======== Transaction B -> A ========");
		var tranForm4 = new TransactionForm(
			walletB.getPrivateKey(),
			walletB.getPublicKey(), 
			walletB.getAddress(),
			walletA.getAddress(),
			new BigDecimal(90));
		sendTransaction(tranForm4);

    }

	public static void sendTransaction(TransactionForm form) {

		// create request to blockchain server
		var transactionReq = new TransactionRequest(
			form.getSenderAddress(),
			form.getRecipientAddress(),
			form.getValue(),
			Instant.now().toEpochMilli());
		transactionReq.signate(form.getSenderPrivateKey(), form.getSenderPublicKey());

		// debug
		System.out.println(transactionReq.marshalJsonPrettyPrinting());
		System.out.println("Is signature valid?: " + transactionReq.verifySignature());

		// request to blockchain server and get response
		// send responst to client
	}

	public static void test() {
		var senderPublicKey = "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEM0mioVbXjoZrtQNWsIUhpGZY08wHdNWXqAIxPkQR8gCRl4nsWrQ4YJd6TwFW1vrafYQ7KqDqaEvVvdNHLQAWfw\u003d\u003d";
		var senderAddress = "1MgsoN669KWhdTqX253aJsahqmr8ZjkWR1";
		var recipirntAddress = "14S5tUuBJkoi1mkPx6XZUZUUsKhzSBqrK5";
		var value = 45.0f;
		var signature = "MEUCIQDr6AfYrTy25bEWcBh0bhItleQI0SdIxEv08QnS0VoX1gIgYhoIItKkwBPJV8aHTkazjjnWXOPmDmtiP4d+LgrhsAA\u003d";

		boolean isValid = SecurityUtil.verifyEcdsaSign(
			senderPublicKey,
			(senderAddress + recipirntAddress + Float.toString(value)).getBytes(),
			signature);
		System.out.println(isValid);
	}

	public static void testAddress() {
		Security.addProvider(new BouncyCastleProvider());

		var privateKeyString = "MD4CAQAwEAYHKoZIzj0CAQYFK4EEAAoEJzAlAgEBBCAFzE1GvP3x7LWRqjHLVrTKdhfLrHinB0a8zL0FMnRnSw==";
		var privateKey = SecurityUtil.decodePrivateKey(privateKeyString);
		var publicKey = SecurityUtil.getPublicKeyFromPriavateKey(privateKey);

		System.out.println(SecurityUtil.encodeKeyToString(publicKey));
		System.out.println(SecurityUtil.getAddressFromPublicKey(publicKey));
	}
}
