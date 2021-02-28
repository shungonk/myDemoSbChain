package com.myexample;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.Security;

import com.google.gson.JsonSyntaxException;
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
        try (var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "GET":
                System.out.println("# Request get balance");
                var query = StringUtil.splitQuery(t.getRequestURI().getQuery());
                var address = query.get("address");
                Result result;
                BigDecimal balance;
                if (address == null) {
                    balance = new BigDecimal("0").setScale(SBChain.TRANSACTION_VALUE_SCALE);
                    result = Result.INCORRECT_QUERY_PARAMETER;
                } else {
                    balance = SBChain.calculateTotalValue(address);
                    result = Result.GET_BARANCE_SUCCESS;
                }
                
                System.out.println(result.getMessage());
                var resGet = StringUtil.doubleEntryJson(
                    "message", result.getMessage(),
                    "balance", StringUtil.formatDecimal(balance, SBChain.TRANSACTION_VALUE_SCALE)); // e.g. "1,234.567890"
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(result.getStatusCodeValue(), resGet.length());
                os.write(resGet.getBytes());
                break;

            default:
                System.out.println("# Invalid HTTP Method");
                t.sendResponseHeaders(405, -1);
            }
        }
    };
    
    public HttpHandler purchaseHandler = (HttpExchange t) -> {
        try (var is = t.getRequestBody(); var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "POST":
                System.out.println("# Request purchase");
                var json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                Result result;
                try {
                    var req = StringUtil.fromJson(json, PurchaseRequest.class);
                    System.out.println(req.marshalJsonPrettyPrinting());

                    if (!req.validateFields())
                        result = Result.MISSING_FIELDS;
                    else if (!req.verifySignature())
                        result = Result.INVALID_SIGNATURE;
                    else if (!req.validateValue())
                        result = Result.NOT_POSITIVE_VALUE;
                    else if (!req.verifyAddress())
                        result = Result.INCONSISTENT_ADDRESS;
                    else
                        result = SBChain.addTransaction(req.getAddress(), req.getValue(), req.getSignature());
                } catch (JsonSyntaxException e) {
                    result = Result.INCORRECT_JSON_CONTENT;
                }

                System.out.println(result.getMessage());
                var resPost = StringUtil.singleEntryJson("message", result.getMessage());
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(result.getStatusCodeValue(), resPost.length());
                os.write(resPost.getBytes());
                break;

            default:
                System.out.println("# Invalid HTTP Method");
                t.sendResponseHeaders(405, -1);
            }
        }
    };

    public HttpHandler chainHandler = (HttpExchange t) -> {
        try (var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "GET":
                System.out.println("# Request get chain");
                var resGet = SBChain.marshalJson();
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(200, resGet.length());
                os.write(resGet.getBytes());
                break;

            default:
                System.out.println("# Invalid HTTP Method");
                t.sendResponseHeaders(405, -1);
            }
        }
    };

    public HttpHandler transactionHandler = (HttpExchange t) -> {
        try (var is = t.getRequestBody(); var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "GET":
                System.out.println("# Request get transaction pool");
                var resGet = SBChain.transactionPoolJson();
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(200, resGet.length());
                os.write(resGet.getBytes());
                break;

            case "POST":
                System.out.println("# Request register transaction");
                var json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                Result result;
                try {
                    var req = StringUtil.fromJson(json, TransactionRequest.class);
                    System.out.println("Catch transaction request");
                    System.out.println(req.marshalJsonPrettyPrinting());
    
                    if (!req.validateFields())
                        result = Result.MISSING_FIELDS;
                    else if (!req.verifySignature())
                        result = Result.INVALID_SIGNATURE;
                    else if (!req.validateValue())
                        result = Result.NOT_POSITIVE_VALUE;
                    else if (!req.verifyAddress())
                        result = Result.INCONSISTENT_ADDRESS;
                    else
                        result = SBChain.addTransaction(
                            req.getSenderAddress(), req.getRecipientAddress(), req.getValue(), req.getSignature());
                } catch (JsonSyntaxException e) {
                    result = Result.INCORRECT_JSON_CONTENT;
                }

                System.out.println(result.getMessage());
                var resPost = StringUtil.singleEntryJson("message", result.getMessage());
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(result.getStatusCodeValue(), resPost.length());
                os.write(resPost.getBytes());
                break;

            default:
                System.out.println("# Invalid HTTP Method");
                t.sendResponseHeaders(405, -1);
            }
        }
    };

    public HttpHandler mineHandler = (HttpExchange t) -> {
        try (var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "POST":
                System.out.println("# Request mining");
                var query = StringUtil.splitQuery(t.getRequestURI().getQuery());
                var address = query.get("address");
                Result result;
                if (address == null)
                    result = Result.INCORRECT_QUERY_PARAMETER;
                else if (!address.equals(SBChain.MINER_ADDRESS))
                    result = Result.MINING_NOT_MINER;
                else
                    result = SBChain.mining();

                System.out.println(result.getMessage());
                var resPost = StringUtil.singleEntryJson("message", result.getMessage());
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(result.getStatusCodeValue(), resPost.length());
                os.write(resPost.getBytes());
                break;

            default:
                System.out.println("# Invalid HTTP Method");
                t.sendResponseHeaders(405, -1);
            }
        }
    };

    public void run() {
        try {
            var host = PropertyUtil.getProperty("host", "localhost");
            var port = Integer.parseInt(PropertyUtil.getProperty("port", "8080"));
            var socketAddress = new InetSocketAddress(host, port);
            var server = HttpServer.create(socketAddress, 0);
            server.createContext("/balance", balanceHandler);
            server.createContext("/purchase", purchaseHandler);
            server.createContext("/chain", chainHandler);
            server.createContext("/transaction", transactionHandler);
            server.createContext("/mine", mineHandler); ///// for demo /////
            server.setExecutor(null);
            server.start();
            System.out.println("Server started on " + socketAddress.toString());
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

        // set mining schedule
        // ScheduledExecutorService miningExecutor = Executors.newScheduledThreadPool(1);
        // miningExecutor.scheduleWithFixedDelay(() -> SBChain.mining(), 0, 1, TimeUnit.DAYS);
    }
}
