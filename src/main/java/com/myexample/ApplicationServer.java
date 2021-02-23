package com.myexample;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.Security;

import com.myexample.blockchain.SBChain;
import com.myexample.common.Result;
import com.myexample.common.TransactionRequest;
import com.myexample.common.utils.PropertyUtil;
import com.myexample.common.utils.StringUtil;
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
                var balance = SBChain.uTXOPool
                    .select(u -> u.belongsTo(address))
                    .totalValue();
                responseHeader.set("Content-Type", "application/json");
                var getResponse = StringUtil.singleEntryJson("balance", balance);
                t.sendResponseHeaders(200, getResponse.length());
                os.write(getResponse.getBytes());
                break;
                
            default:
                var response = Result.HTTP_METHOD_NOT_ALLOWED.toMessageJson();
                t.sendResponseHeaders(405, response.length());
                os.write(response.getBytes());
            }
        }
    };
    
    ////////// for demo //////////
    public HttpHandler purchaseHandler = (HttpExchange t) -> {
        var responseHeader = t.getResponseHeaders();
        try (var is = t.getRequestBody(); var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "POST":
                var query = StringUtil.splitQuery(t.getRequestURI().getQuery());
                var address = query.get("address");
                var value = Float.parseFloat(query.get("value"));
                SBChain.generateTransaction(address, value);

                responseHeader.set("Content-Type", "application/json");
                var postResponse = Result.PURCHASE_SUCCESS.toMessageJson();
                t.sendResponseHeaders(200, postResponse.length());
                os.write(postResponse.getBytes());
                break;

            default:
                var response = Result.HTTP_METHOD_NOT_ALLOWED.toMessageJson();
                t.sendResponseHeaders(405, response.length());
                os.write(response.getBytes());
            }
        }
    };
    //////////////////////////////

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
                var response = Result.HTTP_METHOD_NOT_ALLOWED.toMessageJson();
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
                var transactionRequest = TransactionRequest.fromJson(json);
                Result result = SBChain.acceptTransactionRequest(transactionRequest);

                responseHeader.set("Content-Type", "application/json");
                var postResponse = result.toMessageJson();
                t.sendResponseHeaders(200, postResponse.length());
                os.write(postResponse.getBytes());
                break;

            default:
                var response = Result.HTTP_METHOD_NOT_ALLOWED.toMessageJson();
                t.sendResponseHeaders(405, response.length());
                os.write(response.getBytes());
            }
        }
    };

    public HttpHandler mineHandler = (HttpExchange t) -> {
        var responseHeader = t.getResponseHeaders();
        try (var is = t.getRequestBody(); var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "POST":
                var query = StringUtil.splitQuery(t.getRequestURI().getQuery());
                var address = query.get("address");
                Result result = SBChain.MINER_ADDRESS.equals(address)
                    ? SBChain.mining()
                    : Result.MINING_NOT_MINER;

                responseHeader.set("Content-Type", "application/json");
                var postResponse = result.toMessageJson();
                t.sendResponseHeaders(200, postResponse.length());
                os.write(postResponse.getBytes());
                break;

            default:
                var response = Result.HTTP_METHOD_NOT_ALLOWED.toMessageJson();
                t.sendResponseHeaders(405, response.length());
                os.write(response.getBytes());
            }
        }
    };

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
            e.printStackTrace();
            throw new RuntimeException("Exception occured in Application statring");
        }
    }

    public static void main(String[] args) {
        // add provider for security
        Security.addProvider(new BouncyCastleProvider());
        // demo initialization
        SBChain.generateTransaction(SBChain.MINER_ADDRESS, 10000f);

        new ApplicationServer().run();
    }
}
