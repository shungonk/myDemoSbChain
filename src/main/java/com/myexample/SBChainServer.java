package com.myexample;

import java.security.Security;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.myexample.blockchain.SBChain;
import com.myexample.blockchain.Transaction;
import com.myexample.common.TransactionRequest;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class SBChainServer {

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());

        ExecutorService requestExecutor = Executors.newCachedThreadPool();
        ExecutorService minerExecutor = Executors.newCachedThreadPool();

        System.out.println("============ Set blockchain miner ============");
        SBChain.minerAddress = "temporary_miner_address";

        System.out.println("============ Request ============");
		System.out.println("Transaction base -> A");
        var coinbaseTransaction = new Transaction(
            Transaction.GENESIS_ID,
            "1NZgF6L5yeLTXethCZqa4aG6E4mspnN2ra",
            "14S5tUuBJkoi1mkPx6XZUZUUsKhzSBqrK5",
            100f);
        SBChain.addTransaction(coinbaseTransaction);

		System.out.println("Transaction A -> B");
        var request2 = "{\"senderPublicKey\":\"MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAE2wX86iloHbt9Pf6xAvkLjEhIMoXSez5cZyiKVEktvCv3z4q1mWcl4KxYpSlgQ1zW9rsaWFxC+X8J380CnN653A\u003d\u003d\",\"senderAddress\":\"14S5tUuBJkoi1mkPx6XZUZUUsKhzSBqrK5\",\"recipientAddress\":\"1MgsoN669KWhdTqX253aJsahqmr8ZjkWR1\",\"value\":80.0,\"signature\":\"MEUCIGnLuwbRNGmlH9AfIyQOFPSOOQyVG3sR1U1hYUIU7RK7AiEA0iwY5FKmiIyWpxfmuQDjvL/6QcDGhZ+urJe4Tg9mpM0\u003d\"}";
        requestExecutor.execute(() -> transactionResponse(request2));
        // transactionResponse(request2);

        System.out.println("Mining...");
        minerExecutor.execute(() -> SBChain.mining());
        // SBChain.mining();

		System.out.println("Transaction B -> A");
        var request3 = "{\"senderPublicKey\":\"MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEM0mioVbXjoZrtQNWsIUhpGZY08wHdNWXqAIxPkQR8gCRl4nsWrQ4YJd6TwFW1vrafYQ7KqDqaEvVvdNHLQAWfw\u003d\u003d\",\"senderAddress\":\"1MgsoN669KWhdTqX253aJsahqmr8ZjkWR1\",\"recipientAddress\":\"14S5tUuBJkoi1mkPx6XZUZUUsKhzSBqrK5\",\"value\":45.0,\"signature\":\"MEUCIQDr6AfYrTy25bEWcBh0bhItleQI0SdIxEv08QnS0VoX1gIgYhoIItKkwBPJV8aHTkazjjnWXOPmDmtiP4d+LgrhsAA\u003d\"}";
        requestExecutor.execute(() -> transactionResponse(request3));
        // transactionResponse(request3);

		System.out.println("Transaction B -> A : expect failure by lack of balance");
        var request4 = "{\"senderPublicKey\":\"MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEM0mioVbXjoZrtQNWsIUhpGZY08wHdNWXqAIxPkQR8gCRl4nsWrQ4YJd6TwFW1vrafYQ7KqDqaEvVvdNHLQAWfw\u003d\u003d\",\"senderAddress\":\"1MgsoN669KWhdTqX253aJsahqmr8ZjkWR1\",\"recipientAddress\":\"14S5tUuBJkoi1mkPx6XZUZUUsKhzSBqrK5\",\"value\":90.0,\"signature\":\"MEYCIQDeWH0LnG32y8rVBzcG88fgny1IOM4s5YkSWDKUU4kYfwIhAPR33xy+bfuE/LgGNe3aCMq3uBKO2kAHimgtaZacLKt8\"}";
        requestExecutor.execute(() -> transactionResponse(request4));
        // transactionResponse(request4);

        System.out.println("Mining...");
        minerExecutor.execute(() -> SBChain.mining());
        // SBChain.mining();

        requestExecutor.shutdown();
        minerExecutor.shutdown();

    }

    public static void transactionResponse(String request) {
        // receive request from wallet server
        var transactionRequest = TransactionRequest.fromJson(request);

        // // print fields
        // System.out.println(transactionRequest.getSenderPublicKey());
        // System.out.println(transactionRequest.getSenderAddress());
        // System.out.println(transactionRequest.getRecipientAddress());
        // System.out.println(transactionRequest.getValue());
        // System.out.println(transactionRequest.getSignature());
        
        // accept transaction
        SBChain.acceptTransactionRequest(transactionRequest);

        // response something to wallet server
    }
}
