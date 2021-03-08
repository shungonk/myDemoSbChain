package com.myexample;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.LinkedHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
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

/**
 * クライアントからのリクエストに応答して{@link SBChain}に処理を行うサーバクラス
 * 
 * <table class="striped" style="text-align:left">
 * <caption>リクエストパスに対応するHTTPメソッド及びハンドラ
 * </caption>
 * <thead>
 * <tr>
 * <th scope="col">パス</th>
 * <th scope="col">HTTPメソッド</th>
 * <th scope="col">ハンドラ</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr><th scope="row">/info</th><td>GET</td><td>{@link #infoHandler}</td>
 * <tr><th scope="row">/balance</th><td>GET</td><td>{@link #balanceHandler}</td>
 * <tr><th scope="row">/purchase</th><td>POST</td><td>{@link #purchaseHandler}</td>
 * <tr><th scope="row">/transaction</th><td>POST</td><td>{@link #transactionHandler}</td>
 * <tr><th scope="row">/chain</th><td>GET</td><td>{@link #chainHandler}</td>
 * <tr><th scope="row">/pool</th><td>GET</td><td>{@link #poolHandler}</td>
 * </tbody>
 * </table>
 * 
 * <p>このサーバは全てのリクエストに対してJSON形式のレスポンスボディを返却します。リクエスト
 * を正常に処理された場合、レスポンスのHTTPステータスに200（GETメソッドの場合）、または201
 * （POSTメソッドの場合）を設定します。リクエストを正常に処理できなかった場合や、リクエストが
 * バリデーションによって無効になった場合は、レスポンスのHTTPステータスに400を設定します。
 *
 * <p>サーバーは{@link #run()}メソッドによって起動します。
 * 
 * @author  S.Nakanishi
 * @version 1.0
 * @see     SBChain
 * @see     PurchaseRequest
 * @see     TransactionRequest
 * @see     Result
 */
public class SBChainServer {

    /**
     * ブロックチェーンの設定情報を提供するハンドラ
     * 
     * <p>このハンドラはクエリストリングやリクエストボディは何も必要としません。
     * 
     * <p>GETリクエストに対して、ブロックチェーンの設定情報をJSON形式で提供します。GET以外
     * のリクエストメソッドに対しては、レスポンスのHTTPステータスに405を設定し、レスポンスボ
     * ディには何も設定しません。
     * 
     * <p>レスポンスに設定するJSONは次の様なものです。
     * <pre>
     * {
     *   "Blockchain Name"                    : "SBChain",
     *   "Maximum Transaction Amount"         : "30.000000 SBC",
     *   "Minimum Unit of Transaction Amount" : "0.000001 SBC"
     * }
     * </pre>
     */
    public HttpHandler infoHandler = (HttpExchange t) -> {
        try (var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "GET":
                LogWriter.info("Request get info");
                var infoMap = new LinkedHashMap<String, String>();
                infoMap.put("Blockchain Name", SBChain.BLOCKCHAIN_NAME);
                infoMap.put("Maximum Transaction Amount", 
                    SBChain.TRANSACTION_MAX_AMOUNT
                        .setScale(SBChain.TRANSACTION_AMOUNT_SCALE)
                        .toPlainString()
                     + " SBC");
                infoMap.put("Minimum Unit of Transaction Amount", 
                    BigDecimal.ONE
                        .scaleByPowerOfTen(-SBChain.TRANSACTION_AMOUNT_SCALE)
                        .toPlainString()
                     + " SBC");
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

    /**
     * 指定アドレスがブロックチェーンに所持している残高を提供するハンドラ
     * 
     * <p>このハンドラはクエリストリングでアドレスを受け取る必要があります。アドレスを
     * 受け取ることができなかった場合は、レスポンスのHTTPステータスを400に設定します。
     * 
     * <p>GETリクエストに対して、指定されたアドレスがブロックチェーンに所持している残高を
     * JSON形式で提供します（残高は書式変換された文字列です）。GET以外のリクエストメソッド
     * に対しては、レスポンスのHTTPステータスに405を設定し、レスポンスボディには何も設定
     * しません。
     * 
     * <p>レスポンスに設定するJSONは次の様なものです。
     * <pre>
     * {
     *   "message" : "SUCCESS: Get balance!",
     *   "balance" : "1.250000"
     * }
     * </pre>
     * HTTPステータスが400の処理結果の場合、残高の値は０に設定されます。残高が実際に０で
     * あるかどうかはHTTPステータスやメッセージによって判断します。
     * 
     * @see Result
     */
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
                    balance = BigDecimal.ZERO
                        .setScale(SBChain.TRANSACTION_AMOUNT_SCALE);
                    result = Result.INCORRECT_QUERY_PARAMETER;
                } else {
                    balance = sbChain.calculateTotalAmount(address);
                    result = Result.GET_BARANCE_SUCCESS;
                }
                
                var resGet = StringUtil.makeJson(
                    "message", result.getDetailMessage(),
                    "balance", StringUtil.formatDecimal(
                        balance, SBChain.TRANSACTION_AMOUNT_SCALE));
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
    
    /**
     * ブロックチェーンへの通貨購入リクエストを受け取るハンドラ
     * 
     * <p>このハンドラは次の様なJSON文字列を必要とします。
     * <pre>
     * {
     *   "address"   : "11YonJkss36u34N6tFRNJbVTQyn4zMCij",
     *   "amount"    : 1.000000,
     *   "timeptamp" : 1615092326737,
     *   "publicKey" : "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEOi1K...",
     *   "signature" : "MEQCIC94Mb1QaWaWOfDmyxd8Abiyirngx7wY..."
     * }
     * </pre>
     * ハンドラはこのJSONを{@link PurchaseRequest}オブジェクトに変換して処理を行います。
     * JSONを認識できなかった場合は、レスポンスのHTTPステータスを400に設定します。
     * 
     * <p>POSTリクエストに対して、
     * {@link SBChain#addTransaction(String, BigDecimal, String)}を実行し取引データを
     * 追加します。ただし、処理を実行する前に次の検証を行います。
     * <ul>
     * <li>リクエストのJSON項目に不足がないか
     * <li>リクエストのシグネチャが、有効なものであるか
     * <li>リクエストの金額が、０より大きい値であるか
     * <li>リクエストのアドレスが、パブリックキーに所属するものであるか
     * </ul>
     * 検証がパスしなかった場合、レスポンスのHTTPステータスに400を設定します。POST以外のリク
     * エストメソッドに対しては、レスポンスのHTTPステータスに405を設定し、レスポンスボディには
     * 何も設定しません。
     * 
     * <p>レスポンスに設定するJSONは次の様なものです。
     * <pre>
     * {
     *   "message" :"FAILED: Invalid signature."
     * }
     * </pre>
     * 
     * @see PurchaseRequest
     * @see SBChain#addTransaction(String, BigDecimal, String)
     * @see Result
     */
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
                        result = sbChain.addTransaction(
                            req.getAddress(),
                            req.getAmount(),
                            req.getSignature());
                } catch (JsonSyntaxException e) {
                    result = Result.INCORRECT_JSON_CONTENT;
                }

                LogWriter.info(result.getDetailMessage());
                var resPost = StringUtil.makeJson(
                    "message", result.getDetailMessage());
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
    
    /**
     * ブロックチェーンへの通貨送金リクエストを受け取るハンドラ
     * 
     * <p>このハンドラはPOSTリクエストに次の様なJSON文字列を必要とします。
     * <pre>
     * {
     *   "senderAddress"    : "13ELmMkDwnRT7zr3pMG5jiTGKqzbdjoDWu",
     *   "recipientAddress" : "1CrTGRNVr45EHhU2JsBMxpP9kJoPDtpVVp",
     *   "amount"           : 1.000000,
     *   "timeptamp"        : 1615092326737,
     *   "publicKey"        : "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgA...",
     *   "signature"        : "MEQCIC94Mb1QaWaWOfDmyxd8Abiyirn..."
     * }
     * </pre>
     * このJSONを{@link TransactionRequest}オブジェクトに変換してハンドラは処理を行いま
     * す。JSONを認識できなかった場合は、レスポンスのHTTPステータスを400に設定します。
     * 
     * <p>POSTリクエストに対して、
     * {@link SBChain#addTransaction(String, String, BigDecimal, String)}を実行し
     * 取引データを追加します。ただし、処理を実行する前に次の検証を行います。
     * <ul>
     * <li>リクエストのJSON項目に不足がないか
     * <li>リクエストのシグネチャが、有効なものであるか
     * <li>リクエストの金額が、０より大きい値であるか
     * <li>リクエストのアドレスが、パブリックキーに所属するものであるか
     * </ul>
     * 検証がパスしなかった場合、レスポンスのHTTPステータスに400を設定します。POST以外のリク
     * エストメソッドに対しては、レスポンスのHTTPステータスに405を設定し、レスポンスボディには
     * 何も設定しません。
     * 
     * <p>レスポンスに設定するJSONは次の様なものです。
     * <pre>
     * {
     *   "message" :"FAILED: Requested amount should be positive."
     * }
     * </pre>
     * 
     * @see TransactionRequest
     * @see SBChain#addTransaction(String, String, BigDecimal, String)
     * @see Result
     */
    public HttpHandler transactionHandler = (HttpExchange t) -> {
        try (var is = t.getRequestBody(); var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "POST":
                var json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                LogWriter.info("Request post transaction - " + json);
                Result result;
                try {
                    var req = StringUtil.fromJson(json, TransactionRequest.class);
    
                    if (!req.validateFields())
                        result = Result.MISSING_FIELDS;
                    else if (!req.isRecipientAddressBase58())
                        result = Result.UNEXPECTED_CHARACTER;
                    else if (!req.verifySignature())
                        result = Result.INVALID_SIGNATURE;
                    else if (!req.validateAmount())
                        result = Result.NOT_POSITIVE_AMOUNT;
                    else if (!req.verifyAddress())
                        result = Result.INCONSISTENT_ADDRESS;
                    else
                        result = sbChain.addTransaction(
                            req.getSenderAddress(),
                            req.getRecipientAddress(),
                            req.getAmount(),
                            req.getSignature());
                } catch (JsonSyntaxException e) {
                    result = Result.INCORRECT_JSON_CONTENT;
                }

                LogWriter.info(result.getDetailMessage());
                var resPost = StringUtil.makeJson(
                    "message", result.getDetailMessage());
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

    /**
     * ブロックチェーンに保管されているブロック情報を提供するハンドラ
     * 
     * <p>このハンドラはクエリストリングやリクエストボディは何も必要としません。
     * 
     * <p>GETリクエストに対して、ブロックに保管されているブロック情報を提供します。GET以外
     * のリクエストメソッドに対しては、レスポンスのHTTPステータスに405を設定し、レスポンス
     * ボディには何も設定しません。
     * 
     * <p>レスポンスに設定するJSONは次の様なものです。
     * <pre>
     * [
     *   {
     *     "timestamp":1615095553029,
     *     "previousHash" : "9fbe866a6b8d95d9ff26120870a8bd1...",
     *     "transactions" : [...]
     *     "merkleRoot"   : "0a08e4eabcd93ac6572f9c4cc5e7ac6...",
     *     "nonce"        : 49485
     *   },
     *   ...
     * ]
     * </pre>
     */
    public HttpHandler chainHandler = (HttpExchange t) -> {
        try (var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "GET":
                LogWriter.info("Request get chain");
                var resGet = sbChain.chainJson();
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
    
    /**
     * ブロックチェーンに未保管の取引データ情報を提供するハンドラ
     * 
     * <p>このハンドラはクエリストリングやリクエストボディは何も必要としません。
     * 
     * <p>GETリクエストに対して、ブロックチェーンに未保管の取引データ情報を提供します。GET
     * 以外のリクエストメソッドに対しては、レスポンスのHTTPステータスに405を設定し、レスポ
     * ンスボディには何も設定しません。
     * 
     * <p>レスポンスに設定するJSONは次の様なものです。
     * <pre>
     * [
     *   {
     *     "transactionId"    : "bbba357dfbe2af9ea9a6b4c26aab00b...",
     *     "timestamp"        : 1615095423047,
     *     "senderAddress"    : "13mWCN2puimSoPVWMva5QwMCHS5dx5XkjC",
     *     "recipientAddress" : "1CrTGRNVr45EHhU2JsBMxpP9kJoPDtpVVp",
     *     "amount"           : 8.000000,
     *     "signature"        : "MEUCIHOJ4GsfF8qr+JRvZm9+yt9Ueu1t..."
     *   },
     *   ...
     * ]
     * </pre>
     */
    public HttpHandler poolHandler = (HttpExchange t) -> {
        try (var is = t.getRequestBody(); var os = t.getResponseBody()) {
            switch (t.getRequestMethod()) {
            case "GET":
                LogWriter.info("Request get transaction pool");
                var resGet = sbChain.transactionPoolJson();
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

    /**
     * サーバーを起動します。サーバは環境変数{@code PORT}のポートをリッスンします。リクエス
     * トに対する応答処理は{@link ThreadPoolExecutor}によってスレッド管理されます。
     * 
     * @see ThreadPoolExecutor
     */
    public void run() {
        try {
            var port = System.getenv("PORT");
            var socketAddress = new InetSocketAddress(Integer.parseInt(port));
            var server = HttpServer.create(socketAddress, 0);
            server.createContext("/info", infoHandler);
            server.createContext("/balance", balanceHandler);
            server.createContext("/purchase", purchaseHandler);
            server.createContext("/transaction", transactionHandler);
            server.createContext("/chain", chainHandler);
            server.createContext("/pool", poolHandler);
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            LogWriter.info("Server running on " + socketAddress);
        } catch (IOException e) {
            LogWriter.severe("Failed to start Application", e);
        }
    }

    /**
     * サーバーがリクエストに応答して処理を行う{@link SBChain}オブジェクト
     */
    private static final SBChain sbChain = new SBChain();

    /**
     * コンストラクタ
     */
    public SBChainServer() {}

    /**
     * サーバーのエントリポイントです。
     * @param  args
     *         実行引数
     */

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        sbChain.scheduleAutoMining(3, TimeUnit.MINUTES);
        new SBChainServer().run();
    }
}
