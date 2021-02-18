package com.myexample.httphandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.myexample.blockchain.SBChain;
import com.myexample.common.TransactionRequest;
import com.myexample.common.utils.StringUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class TransactionHandler implements HttpHandler {
    public void handle(HttpExchange t) throws IOException {
        switch (t.getRequestMethod()) {
            case "GET":


                break;

            case "POST":
                try (var is = t.getRequestBody(); var os = t.getResponseBody()) {
                    var json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    var transactionRequest = TransactionRequest.fromJson(json);
                    //debug
                    System.out.println(transactionRequest);
                    var isSuccess = SBChain.acceptTransactionRequest(transactionRequest);
    
                    if (isSuccess) {
                        var response = StringUtil.messageJson("transaction accepted");
                        t.sendResponseHeaders(200, response.length());
                        os.write(response.getBytes());
                    } else {
                        var response = StringUtil.messageJson("transaction not accepted.");
                        t.sendResponseHeaders(400, response.length());
                        os.write(response.getBytes());
                    }
                }
                break;

            default:

        }
    }
}