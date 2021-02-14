package com.myexample.blockchain;

public class SBChainServer {

    public static void main(String[] args) {

    }

    public static void transactionResponse() {
        // accept request from wallet server
        var request = "XXX";
        var transactionRequest = TransactionRequest.unmarshalJson(request);

        // create transaction
        var transaction = new Transaction(
            transactionRequest.getTransactionId(),
            transactionRequest.getSender(),
            transactionRequest.getRecipient(),
            transactionRequest.getValue(),
            transactionRequest.getSignature());
        
        SBChain.addTransaction(transaction);
        
    }
}
