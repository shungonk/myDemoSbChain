package com.myexample;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.LinkedHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonSyntaxException;
import com.myexample.blockchain.Result;
import com.myexample.blockchain.SBChain;
import com.myexample.common.LogWriter;
import com.myexample.common.StringUtil;
import com.myexample.request.PurchaseRequest;
import com.myexample.request.TransactionRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
public class SBChainServer {

    public HttpHandler infoHandler = (HttpExchange t) -> {
        try (var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "GET":
                LogWriter.info("Request get info");
                var infoMap = new LinkedHashMap<String, String>();
                infoMap.put("Blockchain Name", SBChain.BLOCKCHAIN_NAME);
                infoMap.put("Maximum Transaction Amount", 
                    SBChain.TRANSACTION_MAX_AMOUNT.setScale(SBChain.TRANSACTION_AMOUNT_SCALE).toPlainString() + " SBC");
                infoMap.put("Minimum Unit of Transaction Amount", 
                    BigDecimal.ONE.scaleByPowerOfTen(-SBChain.TRANSACTION_AMOUNT_SCALE).toPlainString() + " SBC");
                var resGet = StringUtil.makeJson(infoMap);
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(200, resGet.length());
                os.write(resGet.getBytes());
                break;

            default:
                LogWriter.info("Invalid HTTP Method Requested");
                t.sendResponseHeaders(405, -1);
            }
        }
    };

    public HttpHandler balanceHandler = (HttpExchange t) -> {
        try (var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "GET":
                var query = StringUtil.splitQuery(t.getRequestURI().getQuery());
                var address = query.get("address");
                LogWriter.info("Request get balance - " + address);
                Result result;
                BigDecimal balance;
                if (address == null) {
                    balance = new BigDecimal("0").setScale(SBChain.TRANSACTION_AMOUNT_SCALE);
                    result = Result.INCORRECT_QUERY_PARAMETER;
                } else {
                    balance = SBC.calculateTotalAmount(address);
                    result = Result.GET_BARANCE_SUCCESS;
                }
                
                var resGet = StringUtil.makeJson(
                    "message", result.getStatusAndMessage(),
                    "balance", StringUtil.formatDecimal(balance, SBChain.TRANSACTION_AMOUNT_SCALE)); // e.g. "1,234.567890"
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(result.getStatusCode(), resGet.length());
                os.write(resGet.getBytes());
                break;

            default:
                LogWriter.info("Invalid HTTP Method Requested");
                t.sendResponseHeaders(405, -1);
            }
        }
    };
    
    public HttpHandler purchaseHandler = (HttpExchange t) -> {
        try (var is = t.getRequestBody(); var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "POST":
                var json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                LogWriter.info("Request post purchase - " + json);
                Result result;
                try {
                    var req = StringUtil.fromJson(json, PurchaseRequest.class);

                    if (!req.validateFields())
                        result = Result.MISSING_FIELDS;
                    else if (!req.verifySignature())
                        result = Result.INVALID_SIGNATURE;
                    else if (!req.validateAmount())
                        result = Result.NOT_POSITIVE_AMOUNT;
                    else if (!req.verifyAddress())
                        result = Result.INCONSISTENT_ADDRESS;
                    else
                        result = SBC.addTransaction(req.getAddress(), req.getAmount(), req.getSignature());
                } catch (JsonSyntaxException e) {
                    result = Result.INCORRECT_JSON_CONTENT;
                }

                LogWriter.info(result.getMessage());
                var resPost = StringUtil.makeJson("message", result.getStatusAndMessage());
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(result.getStatusCode(), resPost.length());
                os.write(resPost.getBytes());
                break;

            default:
                LogWriter.info("Invalid HTTP Method Requested");
                t.sendResponseHeaders(405, -1);
            }
        }
    };

    public HttpHandler chainHandler = (HttpExchange t) -> {
        try (var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "GET":
                LogWriter.info("Request get chain");
                var resGet = SBC.chainJson();
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(200, resGet.length());
                os.write(resGet.getBytes());
                break;

            default:
                LogWriter.info("Invalid HTTP Method Requested");
                t.sendResponseHeaders(405, -1);
            }
        }
    };

    public HttpHandler transactionHandler = (HttpExchange t) -> {
        try (var is = t.getRequestBody(); var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "GET":
                LogWriter.info("Request get transaction pool");
                var resGet = SBC.transactionPoolJson();
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(200, resGet.length());
                os.write(resGet.getBytes());
                break;

            case "POST":
                var json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                LogWriter.info("Request post transaction - " + json);
                Result result;
                try {
                    var req = StringUtil.fromJson(json, TransactionRequest.class);
    
                    if (!req.validateFields())
                        result = Result.MISSING_FIELDS;
                    else if (!req.verifySignature())
                        result = Result.INVALID_SIGNATURE;
                    else if (!req.validateAmount())
                        result = Result.NOT_POSITIVE_AMOUNT;
                    else if (!req.verifyAddress())
                        result = Result.INCONSISTENT_ADDRESS;
                    else
                        result = SBC.addTransaction(
                            req.getSenderAddress(), req.getRecipientAddress(), req.getAmount(), req.getSignature());
                } catch (JsonSyntaxException e) {
                    result = Result.INCORRECT_JSON_CONTENT;
                }

                LogWriter.info(result.getMessage());
                var resPost = StringUtil.makeJson("message", result.getStatusAndMessage());
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(result.getStatusCode(), resPost.length());
                os.write(resPost.getBytes());
                break;

            default:
                LogWriter.info("Invalid HTTP Method Requested");
                t.sendResponseHeaders(405, -1);
            }
        }
    };

    public void run() {
        try {
            // local
            // var host = Property.getProperty("host");
            // var port = Property.getProperty("port");
            // var socketAddress = new InetSocketAddress(host, Integer.parseInt(port));
            // Heroku
            var port = System.getenv("PORT");
            var socketAddress = new InetSocketAddress(Integer.parseInt(port));
            var server = HttpServer.create(socketAddress, 0);
            server.createContext("/info", infoHandler);
            server.createContext("/balance", balanceHandler);
            server.createContext("/purchase", purchaseHandler);
            server.createContext("/chain", chainHandler);
            server.createContext("/transaction", transactionHandler);
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            LogWriter.info("Server running on " + socketAddress);
        } catch (IOException e) {
            LogWriter.severe("Failed to start Application", e);
        }
    }

    public static final SBChain SBC = new SBChain();

    public static void main(String[] args) {
        // add provider for security
        Security.addProvider(new BouncyCastleProvider());
        SBC.scheduleAutoMining(5, TimeUnit.MINUTES);

        new SBChainServer().run();
    }
}
