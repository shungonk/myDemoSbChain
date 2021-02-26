package com.myexample.blockchain;

public enum Result {

    // Purchase result
    PURCHASE_SUCCESS(
        Status.SUCCESS, "Purchase successed!"),

    // Transaction result
    TRANSACTION_SUCCESS(
        Status.SUCCESS, "Transaction accepted!"),

    // Mining result
    MINING_NOT_MINER(
        Status.FAILED, "No rights to mine"),
    MINING_POOL_EMPTY(
        Status.FAILED, "Transaction pool is empty"),
    MINING_SUCCESS(
        Status.SUCCESS, "Mining completed!"),

    // Request Vlidation result
    NOT_POSITIVE_VALUE(
        Status.FAILED, "Requested value should be positive"),
    MISSING_FIELDS(
        Status.FAILED, "Request missing field(s)"),
    INVALID_SIGNATURE(
        Status.FAILED, "Invalid signature"),
    INCONSISTENT_ADDRESS(
        Status.FAILED, "Sender address should be consistent with sender public key"),
    SCALE_OVERFLOW(
        Status.FAILED, "Scale overflow (value := unscaledValue × 10-'scale')"),
    TOO_LARGE_VALUE(
        Status.FAILED, "Requested value too large"),
    NOT_ENOUGH_BALANCE(
        Status.FAILED, "Not enough balance"),
    ;

    private enum Status {
        SUCCESS, FAILED, ERROR;
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

    public String getMessage() {
        return status + ": " + message;
    }
}
