package com.myexample.blockchain;

public enum Result {
    // Get balance message
    GET_BARANCE_SUCCESS(
        Status.SUCCESS, "Get balance!"),

    // Purchase result
    PURCHASE_SUCCESS(
        Status.CREATED, "Purchase successed!"),

    // Transaction result
    TRANSACTION_SUCCESS(
        Status.CREATED, "Transaction accepted!"),

    // Mining result
    MINING_NOT_MINER(
        Status.FAILED, "No rights to mine"),
    MINING_POOL_EMPTY(
        Status.FAILED, "Transaction pool is empty"),
    MINING_SUCCESS(
        Status.CREATED, "Mining completed!"),

    // Request Vlidation result
    NOT_POSITIVE_AMOUNT(
        Status.FAILED, "Requested amount should be positive"),
    MISSING_FIELDS(
        Status.FAILED, "Request missing field(s)"),
    INVALID_SIGNATURE(
        Status.FAILED, "Invalid signature"),
    INCONSISTENT_ADDRESS(
        Status.FAILED, "Sender address should be consistent with sender public key"),
    AMOUNT_SCALE_OVERFLOW(
        Status.FAILED, "Amount scale overflow"),
    TOO_LARGE_AMOUNT(
        Status.FAILED, "Requested amount too large"),
    NOT_ENOUGH_BALANCE(
        Status.FAILED, "Not enough balance"),
    SIGNATURE_ALREADY_CONSUMED(
        Status.FAILED, "Signature is already consumed"),
    
    // HTTP Hundle message
    INCORRECT_JSON_CONTENT(
        Status.FAILED, "Incorrect json content"),
    INCORRECT_QUERY_PARAMETER(
        Status.FAILED, "Incorrect query parameters"),
    ;

    private enum Status {
        SUCCESS(200),
        CREATED(201),
        FAILED(400), ;
        private int statusCode; // http status code
        private Status(int statusCode) {
            this.statusCode = statusCode;
        }
        private int getStatusCode() {
            return statusCode;
        }
    }

    private Status status;
    private String message;

    private Result(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isCreated() {
        return status == Status.CREATED;
    }

    public boolean isFailed() {
        return status == Status.FAILED;
    }

    public int getStatusCode() {
        return status.getStatusCode();
    }

    public String getMessage() {
        return message;
    }

    public String getStatusAndMessage() {
        if (isFailed())
            return "FAILED: " + message;
        else
            return "SUCCESS: " + message;
    }
}
