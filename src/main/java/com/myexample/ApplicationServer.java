package com.myexample;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Objects;

import com.myexample.blockchain.Result;
import com.myexample.blockchain.SBChain;
import com.myexample.request.PurchaseRequest;
import com.myexample.request.TransactionRequest;
import com.myexample.utils.PropertyUtil;
import com.myexample.utils.StringUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
public class ApplicationServer {

    public HttpHandler balanceHandler = (HttpExchange t) -> {
        var responseHeader = t.getResponseHeaders();
        try (var is = t.getRequestBody(); var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "GET":
                var query = StringUtil.splitQuery(t.getRequestURI().getQuery());
                var address = query.get("address");
                var balance = SBChain.calculateTotalValue(address);
                responseHeader.set("Content-Type", "application/json");
                var getResponse = StringUtil.singleEntryJson("balance", StringUtil.formatDecimal(balance)); // e.g. "12,345.678901"
                t.sendResponseHeaders(200, getResponse.length());
                os.write(getResponse.getBytes());
                break;

            default:
                var response = StringUtil.messageJson("Method Not Allowed");
                t.sendResponseHeaders(405, response.length());
                os.write(response.getBytes());
            }
        }
    };
    
    public HttpHandler purchaseHandler = (HttpExchange t) -> {
        var responseHeader = t.getResponseHeaders();
        try (var is = t.getRequestBody(); var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "POST":
                var json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                var req = PurchaseRequest.fromJson(json);
                System.out.println("Catch purchase request");
                System.out.println(req.marshalJsonPrettyPrinting());

                Result result;
                if (!req.validatePurchaseRequest())
                    result = Result.MISSING_FIELDS;
                else if (!req.verifySignature())
                    result = Result.INVALID_SIGNATURE;
                else if (!req.validateValue())
                    result = Result.NOT_POSITIVE_VALUE;
                else if (!req.veritfyAddress())
                    result = Result.INCONSISTENT_ADDRESS;
                else
                    result = SBChain.addTransaction(req.getAddress(), req.getValue(), req.getSignature());

                System.out.println(result.getMessage());
                responseHeader.set("Content-Type", "application/json");
                var postResponse = StringUtil.messageJson(result.getMessage());
                t.sendResponseHeaders(201, postResponse.length());
                os.write(postResponse.getBytes());
                break;

            default:
                var response = StringUtil.messageJson("Method Not Allowed");
                t.sendResponseHeaders(405, response.length());
                os.write(response.getBytes());
            }
        }
    };

    public HttpHandler chainHandler = (HttpExchange t) -> {
        var responseHeader = t.getResponseHeaders();
        try (var is = t.getRequestBody(); var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "GET":
                responseHeader.set("Content-Type", "application/json");
                var getResponse = SBChain.marshalJson();
                t.sendResponseHeaders(200, getResponse.length());
                os.write(getResponse.getBytes());
                break;

            default:
                var response = StringUtil.messageJson("Method Not Allowed");
                t.sendResponseHeaders(405, response.length());
                os.write(response.getBytes());
            }
        }
    };

    public HttpHandler transactionHandler = (HttpExchange t) -> {
        var responseHeader = t.getResponseHeaders();
        try (var is = t.getRequestBody(); var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "GET":
                responseHeader.set("Content-Type", "application/json");
                var getResponse = SBChain.transactionPoolJson();
                t.sendResponseHeaders(200, getResponse.length());
                os.write(getResponse.getBytes());
                break;

            case "POST":
                var json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                var req = TransactionRequest.fromJson(json);
                System.out.println("Catch transaction request");
                System.out.println(req.marshalJsonPrettyPrinting());

                Result result;
                if (!req.validateTransactionRequest())
                    result = Result.MISSING_FIELDS;
                else if (!req.verifySignature())
                    result = Result.INVALID_SIGNATURE;
                else if (!req.validateValue())
                    result = Result.NOT_POSITIVE_VALUE;
                else if (!req.veritfyAddress())
                    result = Result.INCONSISTENT_ADDRESS;
                else
                    result = SBChain.addTransaction(
                        req.getSenderAddress(), req.getRecipientAddress(), req.getValue(), req.getSignature());

                System.out.println(result.getMessage());
                responseHeader.set("Content-Type", "application/json");
                var postResponse = StringUtil.messageJson(result.getMessage());
                t.sendResponseHeaders(201, postResponse.length());
                os.write(postResponse.getBytes());
                break;

            default:
                var response = StringUtil.messageJson("Method Not Allowed");
                t.sendResponseHeaders(405, response.length());
                os.write(response.getBytes());
            }
        }
    };

    ////////// for demo //////////
    public HttpHandler mineHandler = (HttpExchange t) -> {
        var responseHeader = t.getResponseHeaders();
        try (var is = t.getRequestBody(); var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "POST":
                var query = StringUtil.splitQuery(t.getRequestURI().getQuery());
                var address = query.get("address");
                Result result = Objects.equals(address, SBChain.MINER_ADDRESS)
                    ? SBChain.mining()
                    : Result.MINING_NOT_MINER;

                responseHeader.set("Content-Type", "application/json");
                var postResponse = StringUtil.messageJson(result.getMessage());
                t.sendResponseHeaders(201, postResponse.length());
                os.write(postResponse.getBytes());
                break;

            default:
                var response = StringUtil.messageJson("Method Not Allowed");
                t.sendResponseHeaders(405, response.length());
                os.write(response.getBytes());
            }
        }
    };
    //////////////////////////////

    public void run() {
        try {
            var host = PropertyUtil.getProperty("host", "localhost");
            var port = Integer.parseInt(PropertyUtil.getProperty("port", "8080"));
            var server = HttpServer.create(new InetSocketAddress(host, port), 0);
            server.createContext("/balance", balanceHandler);
            server.createContext("/purchase", purchaseHandler);
            server.createContext("/chain", chainHandler);
            server.createContext("/transaction", transactionHandler);
            server.createContext("/mine", mineHandler);
            server.setExecutor(null);
            server.start();
            System.out.println("Server started on port " + port);
        } catch (IOException e) {
            System.out.println("Failed to start Application");
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        // add provider for security
        Security.addProvider(new BouncyCastleProvider());
        SBChain.loadChain();
        SBChain.loadTransactionPool();

        new ApplicationServer().run();
    }
}
