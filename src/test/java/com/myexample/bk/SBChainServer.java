package com.myexample.bk;

import java.security.Security;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.myexample.blockchain.SBChain;
import com.myexample.common.TransactionRequest;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class SBChainServer {

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());

        ExecutorService executor = Executors.newCachedThreadPool();
        var request2 = "{\"senderPublicKey\":\"MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEeJ9hfzw+tkrBV47QM67htmK31Vg/otuDudsMFIRpVKWsqr+1kJsnifB4gpjWTIyc4UJIdaQsAJ000q+TFfPJlQ\u003d\u003d\",\"senderAddress\":\"1K8if6hiWRapKLxWAdVcpys2QbReBD5P6p\",\"recipientAddress\":\"1MU4DNa19NyWBLgBmEEraySiKSk8u4Zhbw\",\"value\":80.0,\"signature\":\"MEUCIBZWGACHZJLNTqE7nZ+JsQ3q8+KDoRAcnr/ijfa4LOfBAiEA1ZXxdrvs+XkrrQk7HZG5eGcQXIWThaNYUd6TQ68yDCI\u003d\"}";
        var request3 = "{\"senderPublicKey\":\"MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEx/4qh5VHgEpCiuVEpTWNVFB3Y/+IQUk2q4kjgpMDYWEvy2W/Iq9POMIv/Fk4SZPSD8807SZD6bNx8ZPkpk0ZCQ\u003d\u003d\",\"senderAddress\":\"1MU4DNa19NyWBLgBmEEraySiKSk8u4Zhbw\",\"recipientAddress\":\"1K8if6hiWRapKLxWAdVcpys2QbReBD5P6p\",\"value\":45.0,\"signature\":\"MEYCIQDin/SAZSz6vbhdBSLoqeRx4Aqav4IgJWfsTeGefopEWQIhAKsgRa0MQthkM+uqZirnI7OF9nOzLk7TXmuvpj4hR6Ha\"}";
        var request4 = "{\"senderPublicKey\":\"MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEx/4qh5VHgEpCiuVEpTWNVFB3Y/+IQUk2q4kjgpMDYWEvy2W/Iq9POMIv/Fk4SZPSD8807SZD6bNx8ZPkpk0ZCQ\u003d\u003d\",\"senderAddress\":\"1MU4DNa19NyWBLgBmEEraySiKSk8u4Zhbw\",\"recipientAddress\":\"1K8if6hiWRapKLxWAdVcpys2QbReBD5P6p\",\"value\":90.0,\"signature\":\"MEYCIQCgzsRhLFgpFMAaBso8D3QF3NSotI1vV2IQS6PMGqHTaQIhALLw5LXKYzqHqvCs3vIQcbD/PNWTwEN+dcXjM1Fy77ji\"}";
        
        try {
            executor.execute(() -> SBChain.addGenesisTransaction(SBChain.MINER_ADDRESS, 100f));
            executor.execute(() -> transactionResponse(request2));
    
            Thread.sleep(3000);
            executor.execute(() -> SBChain.mining());

            Thread.sleep(3000);
            executor.execute(() -> transactionResponse(request3));
            executor.execute(() -> transactionResponse(request4));

            Thread.sleep(3000);
            executor.execute(() -> SBChain.mining());
    
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    public static void transactionResponse(String request) {
        // receive request from wallet server
        var transactionRequest = TransactionRequest.fromJson(request);
        // accept transaction
        SBChain.acceptTransactionRequest(transactionRequest);
        // response something to wallet server
    }
}
