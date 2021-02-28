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
import com.myexample.utils.LogWriter;
import com.myexample.utils.Property;
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
                LogWriter.info("# Request get balance");
                var query = StringUtil.splitQuery(t.getRequestURI().getQuery());
                var address = query.get("address");
                Result result;
                BigDecimal balance;
                if (address == null) {
                    balance = new BigDecimal("0").setScale(SBChain.TRANSACTION_AMOUNT_SCALE);
                    result = Result.INCORRECT_QUERY_PARAMETER;
                } else {
                    balance = SBChain.calculateTotalAmount(address);
                    result = Result.GET_BARANCE_SUCCESS;
                }
                
                var resGet = StringUtil.doubleEntryJson(
                    "message", result.getStatusAndMessage(),
                    "balance", StringUtil.formatDecimal(balance, SBChain.TRANSACTION_AMOUNT_SCALE)); // e.g. "1,234.567890"
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(result.getStatusCode(), resGet.length());
                os.write(resGet.getBytes());
                break;

            default:
                LogWriter.info("# Invalid HTTP Method Requested");
                t.sendResponseHeaders(405, -1);
            }
        }
    };
    
    public HttpHandler purchaseHandler = (HttpExchange t) -> {
        try (var is = t.getRequestBody(); var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "POST":
                LogWriter.info("# Request post purchase");
                var json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                Result result;
                try {
                    var req = StringUtil.fromJson(json, PurchaseRequest.class);
                    LogWriter.info("\n" + req.marshalJsonPrettyPrinting());

                    if (!req.validateFields())
                        result = Result.MISSING_FIELDS;
                    else if (!req.verifySignature())
                        result = Result.INVALID_SIGNATURE;
                    else if (!req.validateAmount())
                        result = Result.NOT_POSITIVE_AMOUNT;
                    else if (!req.verifyAddress())
                        result = Result.INCONSISTENT_ADDRESS;
                    else
                        result = SBChain.addTransaction(req.getAddress(), req.getAmount(), req.getSignature());
                } catch (JsonSyntaxException e) {
                    result = Result.INCORRECT_JSON_CONTENT;
                }

                LogWriter.info(result.getMessage());
                var resPost = StringUtil.singleEntryJson("message", result.getStatusAndMessage());
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(result.getStatusCode(), resPost.length());
                os.write(resPost.getBytes());
                break;

            default:
                LogWriter.info("# Invalid HTTP Method Requested");
                t.sendResponseHeaders(405, -1);
            }
        }
    };

    public HttpHandler chainHandler = (HttpExchange t) -> {
        try (var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "GET":
                LogWriter.info("# Request get chain");
                var resGet = SBChain.marshalJson();
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(200, resGet.length());
                os.write(resGet.getBytes());
                break;

            default:
                LogWriter.info("# Invalid HTTP Method Requested");
                t.sendResponseHeaders(405, -1);
            }
        }
    };

    public HttpHandler transactionHandler = (HttpExchange t) -> {
        try (var is = t.getRequestBody(); var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "GET":
                LogWriter.info("# Request get transaction pool");
                var resGet = SBChain.transactionPoolJson();
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(200, resGet.length());
                os.write(resGet.getBytes());
                break;

            case "POST":
                LogWriter.info("# Request post transaction");
                var json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                Result result;
                try {
                    var req = StringUtil.fromJson(json, TransactionRequest.class);
                    LogWriter.info("\n" + req.marshalJsonPrettyPrinting());
    
                    if (!req.validateFields())
                        result = Result.MISSING_FIELDS;
                    else if (!req.verifySignature())
                        result = Result.INVALID_SIGNATURE;
                    else if (!req.validateAmount())
                        result = Result.NOT_POSITIVE_AMOUNT;
                    else if (!req.verifyAddress())
                        result = Result.INCONSISTENT_ADDRESS;
                    else
                        result = SBChain.addTransaction(
                            req.getSenderAddress(), req.getRecipientAddress(), req.getAmount(), req.getSignature());
                } catch (JsonSyntaxException e) {
                    result = Result.INCORRECT_JSON_CONTENT;
                }

                LogWriter.info(result.getMessage());
                var resPost = StringUtil.singleEntryJson("message", result.getStatusAndMessage());
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(result.getStatusCode(), resPost.length());
                os.write(resPost.getBytes());
                break;

            default:
                LogWriter.info("# Invalid HTTP Method Requested");
                t.sendResponseHeaders(405, -1);
            }
        }
    };

    public HttpHandler mineHandler = (HttpExchange t) -> {
        try (var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "POST":
                LogWriter.info("# Request post mining");
                var query = StringUtil.splitQuery(t.getRequestURI().getQuery());
                var address = query.get("address");
                Result result;
                if (address == null)
                    result = Result.INCORRECT_QUERY_PARAMETER;
                else if (!address.equals(SBChain.MINER_ADDRESS))
                    result = Result.MINING_NOT_MINER;
                else
                    result = SBChain.mining();

                LogWriter.info(result.getMessage());
                var resPost = StringUtil.singleEntryJson("message", result.getStatusAndMessage());
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(result.getStatusCode(), resPost.length());
                os.write(resPost.getBytes());
                break;

            default:
                LogWriter.info("# Invalid HTTP Method Requested");
                t.sendResponseHeaders(405, -1);
            }
        }
    };

    public void run() {
        try {
            var host = Property.getProperty("host");
            var port = Integer.parseInt(Property.getProperty("port"));
            var socketAddress = new InetSocketAddress(host, port);
            var server = HttpServer.create(socketAddress, 0);
            server.createContext("/balance", balanceHandler);
            server.createContext("/purchase", purchaseHandler);
            server.createContext("/chain", chainHandler);
            server.createContext("/transaction", transactionHandler);
            server.createContext("/mine", mineHandler); ///// for demo /////
            server.setExecutor(null);
            server.start();
            LogWriter.info("Server running on " + socketAddress);
        } catch (IOException e) {
            LogWriter.severe("Failed to start Application", e);
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
