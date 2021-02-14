package com.myexample.blockchain;

import java.security.Security;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class SBChainServer {

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());

        // System.out.println("============ Request ============");
		// System.out.println("Transaction base -> A");
        // var request1 = "{\"senderPublicKey\":\"MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEyqvIaJQ4fkDbPsFNpwA0ljDlz9EPWDvGXcarzphOtZHzWipqLFOAXkyJi4ozFWly\",\"recipientPublicKey\":\"MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEdx4mJixwAMGEKRZ8NnEex5u0Gdark6vkQfSPaxcZJVXd6PtIPx06i3EP2+6QpuuN\",\"value\":100.0,\"signature\":\"MDUCGQC1Q5AsOm9JPxqv57rDfGjXlk3mCGWLpD0CGH8fysajLUJ0Hh9tJPUG6HuQMe+5x1AXXA\u003d\u003d\",\"transactionId\":\"0\"}";
        // transactionResponse(request1);

		// System.out.println("Transaction A -> B");
        // var request2 = "{\"senderPublicKey\":\"MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEdx4mJixwAMGEKRZ8NnEex5u0Gdark6vkQfSPaxcZJVXd6PtIPx06i3EP2+6QpuuN\",\"recipientPublicKey\":\"MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEuN/IG9cgkxdeBojZWmMavXGXQrywH4275qTfgB/S3GIulOXfWOZuATMVx/IFC3l8\",\"value\":80.0,\"signature\":\"MDQCGCYjZYuR9DWo8i6SlR91crmVFw4MhYmu7wIYdqGcF19TNzyMK+SkbznctxV7HwOZbg9h\",\"transactionId\":\"904f48e9c656bfb8a5407b9d1669eb06d79866d6b76ae68e80d90de806f3a44b\"}";
        // transactionResponse(request2);

        // System.out.println("Mining...");
        // SBChain.mining();


		// System.out.println("Transaction B -> A");
        // var request3 = "{\"senderPublicKey\":\"MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEuN/IG9cgkxdeBojZWmMavXGXQrywH4275qTfgB/S3GIulOXfWOZuATMVx/IFC3l8\",\"recipientPublicKey\":\"MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEdx4mJixwAMGEKRZ8NnEex5u0Gdark6vkQfSPaxcZJVXd6PtIPx06i3EP2+6QpuuN\",\"value\":45.0,\"signature\":\"MDUCGDyMTW9bBQ9XP19puLtJMyG+JfWV2V52TAIZAOFvLbkLGLddwKL/SUGp4naRCF8uL5ViLw\u003d\u003d\",\"transactionId\":\"a7a799fac2a61298e32c628baa75b766b8abf0fe328677cfb466655848ac78fd\"}";
        // transactionResponse(request3);

		// System.out.println("Transaction B -> A : expect failure by lack of balance");
        // var request4 = "{\"senderPublicKey\":\"MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEuN/IG9cgkxdeBojZWmMavXGXQrywH4275qTfgB/S3GIulOXfWOZuATMVx/IFC3l8\",\"recipientPublicKey\":\"MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEdx4mJixwAMGEKRZ8NnEex5u0Gdark6vkQfSPaxcZJVXd6PtIPx06i3EP2+6QpuuN\",\"value\":90.0,\"signature\":\"MDQCGBiOaJIvbxCShkKJAaQQRWJlS/uKTlQlRwIYMTWt0AjTQayDPHRTPvkzwASOIfE35nNl\",\"transactionId\":\"56c88f5ecd1eead87c63fb976d1aff20d846de2691b79ce8a1e2e1b6c8ad4bf3\"}";
        // transactionResponse(request4);

		// System.out.println("Transaction B -> A : expect failure by invaid signature(recipient public key) ");
        // var request5 = "{\"senderPublicKey\":\"MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEuN/IG9cgkxdeBojZWmMavXGXQrywH4275qTfgB/S3GIulOXfWOZuATMVx/IFC3l8\",\"recipientPublicKey\":\"IamBugMEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEdx4mJixwAMGEKRZ8NnEex5u0Gdark6vkQfSPaxcZJVXd6PtIPx06i3EP2+6QpuuN\",\"value\":90.0,\"signature\":\"MDQCGBiOaJIvbxCShkKJAaQQRWJlS/uKTlQlRwIYMTWt0AjTQayDPHRTPvkzwASOIfE35nNl\",\"transactionId\":\"56c88f5ecd1eead87c63fb976d1aff20d846de2691b79ce8a1e2e1b6c8ad4bf3\"}";
        // transactionResponse(request5);

        // System.out.println("Mining...");
        // SBChain.mining();

        ExecutorService requestExecutor = Executors.newCachedThreadPool();
        ExecutorService minerExecutor = Executors.newCachedThreadPool();

        System.out.println("============ Request ============");
		System.out.println("Transaction base -> A");
        var request1 = "{\"senderPublicKey\":\"MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEyqvIaJQ4fkDbPsFNpwA0ljDlz9EPWDvGXcarzphOtZHzWipqLFOAXkyJi4ozFWly\",\"recipientPublicKey\":\"MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEdx4mJixwAMGEKRZ8NnEex5u0Gdark6vkQfSPaxcZJVXd6PtIPx06i3EP2+6QpuuN\",\"value\":100.0,\"signature\":\"MDUCGQC1Q5AsOm9JPxqv57rDfGjXlk3mCGWLpD0CGH8fysajLUJ0Hh9tJPUG6HuQMe+5x1AXXA\u003d\u003d\",\"transactionId\":\"0\"}";
        transactionResponse(request1);

		System.out.println("Transaction A -> B");
        var request2 = "{\"senderPublicKey\":\"MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEdx4mJixwAMGEKRZ8NnEex5u0Gdark6vkQfSPaxcZJVXd6PtIPx06i3EP2+6QpuuN\",\"recipientPublicKey\":\"MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEuN/IG9cgkxdeBojZWmMavXGXQrywH4275qTfgB/S3GIulOXfWOZuATMVx/IFC3l8\",\"value\":80.0,\"signature\":\"MDQCGCYjZYuR9DWo8i6SlR91crmVFw4MhYmu7wIYdqGcF19TNzyMK+SkbznctxV7HwOZbg9h\",\"transactionId\":\"904f48e9c656bfb8a5407b9d1669eb06d79866d6b76ae68e80d90de806f3a44b\"}";
        requestExecutor.execute(() -> transactionResponse(request2));

        System.out.println("Mining...");
        minerExecutor.execute(() -> SBChain.mining());


		System.out.println("Transaction B -> A");
        var request3 = "{\"senderPublicKey\":\"MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEuN/IG9cgkxdeBojZWmMavXGXQrywH4275qTfgB/S3GIulOXfWOZuATMVx/IFC3l8\",\"recipientPublicKey\":\"MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEdx4mJixwAMGEKRZ8NnEex5u0Gdark6vkQfSPaxcZJVXd6PtIPx06i3EP2+6QpuuN\",\"value\":45.0,\"signature\":\"MDUCGDyMTW9bBQ9XP19puLtJMyG+JfWV2V52TAIZAOFvLbkLGLddwKL/SUGp4naRCF8uL5ViLw\u003d\u003d\",\"transactionId\":\"a7a799fac2a61298e32c628baa75b766b8abf0fe328677cfb466655848ac78fd\"}";
        requestExecutor.execute(() -> transactionResponse(request3));

		System.out.println("Transaction B -> A : expect failure by lack of balance");
        var request4 = "{\"senderPublicKey\":\"MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEuN/IG9cgkxdeBojZWmMavXGXQrywH4275qTfgB/S3GIulOXfWOZuATMVx/IFC3l8\",\"recipientPublicKey\":\"MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEdx4mJixwAMGEKRZ8NnEex5u0Gdark6vkQfSPaxcZJVXd6PtIPx06i3EP2+6QpuuN\",\"value\":90.0,\"signature\":\"MDQCGBiOaJIvbxCShkKJAaQQRWJlS/uKTlQlRwIYMTWt0AjTQayDPHRTPvkzwASOIfE35nNl\",\"transactionId\":\"56c88f5ecd1eead87c63fb976d1aff20d846de2691b79ce8a1e2e1b6c8ad4bf3\"}";
        requestExecutor.execute(() -> transactionResponse(request4));

		System.out.println("Transaction B -> A : expect failure by invaid signature(recipient public key) ");
        var request5 = "{\"senderPublicKey\":\"MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEuN/IG9cgkxdeBojZWmMavXGXQrywH4275qTfgB/S3GIulOXfWOZuATMVx/IFC3l8\",\"recipientPublicKey\":\"IamBugMEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEdx4mJixwAMGEKRZ8NnEex5u0Gdark6vkQfSPaxcZJVXd6PtIPx06i3EP2+6QpuuN\",\"value\":90.0,\"signature\":\"MDQCGBiOaJIvbxCShkKJAaQQRWJlS/uKTlQlRwIYMTWt0AjTQayDPHRTPvkzwASOIfE35nNl\",\"transactionId\":\"56c88f5ecd1eead87c63fb976d1aff20d846de2691b79ce8a1e2e1b6c8ad4bf3\"}";
        requestExecutor.execute(() -> transactionResponse(request5));

        System.out.println("Mining...");
        minerExecutor.execute(() -> SBChain.mining());

        requestExecutor.shutdown();
        minerExecutor.shutdown();

    }

    public static void transactionResponse(String request) {
        // accept request from wallet server
        var transactionRequest = TransactionRequest.fromJson(request);

        // create transaction
        var transaction = new Transaction(
            transactionRequest.getTransactionId(),
            transactionRequest.getSender(),
            transactionRequest.getRecipient(),
            transactionRequest.getValue(),
            transactionRequest.getSignature());
        
        var result = SBChain.addTransaction(transaction);

        // response something to wallet server
    }
}
