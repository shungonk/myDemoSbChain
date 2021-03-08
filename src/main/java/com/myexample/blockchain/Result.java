package com.myexample.blockchain;

/**
 * リクエスト結果の列挙型
 * 
 * <p>内部にHTTPステータス、リクエスト結果のメッセージを保持します。
 * 
 * @author  S.Nakanishi
 * @version 1.0
 */
public enum Result {

    // -- Success --

    /**
     * 残高の取得に成功した場合
     */
    GET_BARANCE_SUCCESS(
        Status.SUCCESS, "Get balance!"),

    /**
     * 通貨の購入に成功した場合
     */
    PURCHASE_SUCCESS(
        Status.CREATED, "Purchase successed!"),

    /**
     * 通貨の送金に成功した場合
     */
    TRANSACTION_SUCCESS(
        Status.CREATED, "Transaction accepted!"),

    // -- Validation Failure --

    /**
     * リクエストの金額が、０より大きくない場合
     */
    NOT_POSITIVE_AMOUNT(
        Status.FAILED, "Requested amount should be positive."),
    
    /**
     * リクエストの項目に不足があった場合
     */
    MISSING_FIELDS(
        Status.FAILED, "Request missing field(s)."),

    /**
     * リクエストの項目値が、想定外の文字を含んでいた場合
     */
    UNEXPECTED_CHARACTER(
        Status.FAILED, "Request contains unexpected character(s)."),

    /**
     * リクエストのシグネチャが、認証に失敗した場合
     */
    INVALID_SIGNATURE(
        Status.FAILED, "Invalid signature."),

    /**
     * リクエストの送金元アドレスが、パブリックキーに所属しないものである場合
     */
    INCONSISTENT_ADDRESS(
        Status.FAILED, "Sender address should be consistent with public key."),

    /**
     * リクエストの金額の小数桁のスケールが、ブロックチェーンの設定より大きかった場合
     */
    AMOUNT_SCALE_OVERFLOW(
        Status.FAILED, "Amount scale overflow."),

    /**
     * リクエストの金額が、ブロックチェーンの設定より大きかった場合
     */
    TOO_LARGE_AMOUNT(
        Status.FAILED, "Requested amount too large."),

    /**
     * リクエストの送金元アドレスの残高が、リクエストの金額より少なかった場合
     */
    NOT_ENOUGH_BALANCE(
        Status.FAILED, "Not enough balance."),

    /**
     * リクエストのシグネチャが、過去に使用されたシグネチャと重複した場合
     */
    SIGNATURE_ALREADY_CONSUMED(
        Status.FAILED, "Signature is already consumed."),
    
    // -- HTTP Hundle Failure --

    /**
     * リクエストボディが、JSONと認識できなかった場合
     */
    INCORRECT_JSON_CONTENT(
        Status.FAILED, "Incorrect json content."),

    /**
     * リクエストのクエリストリングに、ハンドラが想定しているキーが含まれていなかった場合
     */
    INCORRECT_QUERY_PARAMETER(
        Status.FAILED, "Incorrect query parameters."),
    ;

    /**
     * HTTPステータスの列挙型
     */
    private enum Status {
        SUCCESS(200),
        CREATED(201),
        FAILED(400), ;
        private int statusCode;
        private Status(int statusCode) {
            this.statusCode = statusCode;
        }
        private int getStatusCode() {
            return statusCode;
        }
    }

    /**
     * HTTPステータスコード
     */
    private Status status;

    /**
     * リクエスト結果を表すメッセージ
     */
    private String message;

    private Result(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * リクエスト結果のHTTPステータスがSUCCESS(200)であるかどうかを判定します。
     * 
     * @return {@code ture} リクエスト結果のHTTPステータスがSUCCESS(200)である場合
     */
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    /**
     * リクエスト結果のHTTPステータスがCREATED(201)であるかどうかを判定します。
     * 
     * @return {@code ture} リクエスト結果のHTTPステータスがCREATED(201)である場合
     */
    public boolean isCreated() {
        return status == Status.CREATED;
    }

    /**
     * リクエスト結果のHTTPステータスがFAILED(400)であるかどうかを判定します。
     * 
     * @return {@code ture} リクエスト結果のHTTPステータスがFAILED(400)である場合
     */
    public boolean isFailed() {
        return status == Status.FAILED;
    }

    /**
     * リクエスト結果のHTTPステータスコードを取得します。
     * 
     * @return リクエスト結果のHTTPステータスコード
     */
    public int getStatusCode() {
        return status.getStatusCode();
    }

    /**
     * リクエスト結果の詳細メッセージを取得します。
     * 
     * @return リクエスト結果の詳細メッセージ
     */
    public String getDetailMessage() {
        return status.toString() + ": " + message;
    }
}
