package com.myexample.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.myexample.blockchain.SBChain;
import com.myexample.common.TransactionRequest;
import com.myexample.common.utils.StringUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class TransactionHandler implements HttpHandler {
    public void handle(HttpExchange t) throws IOException {
        var responseHeader = t.getResponseHeaders();
        try (var is = t.getRequestBody(); var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "GET":


                break;

            case "POST":
                var json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                var transactionRequest = TransactionRequest.fromJson(json);
                var isSuccess = SBChain.acceptTransactionRequest(transactionRequest);

                responseHeader.add("Content-Type", "application/json");
                var postResponse = isSuccess
                    ? StringUtil.messageJson("Transaction accepted!")
                    : StringUtil.messageJson("Unacceptable transaction.");
                t.sendResponseHeaders(200, postResponse.length());
                os.write(postResponse.getBytes());
                break;

            default:
                var response = "Invalid HTTP Method";
                t.sendResponseHeaders(400, response.length());
                os.write(response.getBytes());
            }
        }
    }
}