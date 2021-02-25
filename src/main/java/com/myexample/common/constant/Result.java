package com.myexample.common.constant;

import com.myexample.common.utils.StringUtil;

public enum Result {

    // Purchase result
    PURCHASE_SUCCESS(
        Status.SUCCESS, "Purchase successed!"),

    // Transaction result
    TRANSACTION_NOT_ENOUGH_BALANCE(
        Status.FAILED, "Not enough balance"),
    TRANSACTION_TOO_SMALL_VALUE(
        Status.FAILED, "Transaction value too small"),
    TRANSACTION_TOO_LARGE_VALUE(
        Status.FAILED, "Transaction value too large"),
    TRANSACTION_SUCCESS(
        Status.SUCCESS, "Transaction successfully accepted!"),

    // Mining result
    MINING_NOT_MINER(
        Status.FAILED, "No rights to mine"),
    MINING_POOL_EMPTY(
        Status.FAILED, "Transaction pool is empty"),
    MINING_SUCCESS(
        Status.SUCCESS, "Mining Completed!"),

    // Vlidation result
    NOT_POSITIVE_VALUE(
        Status.FAILED, "Requested value is not positive"),
    MISSING_FIELDS(
        Status.FAILED, "Request missing field(s)"),
    INVALID_SIGNATURE(
        Status.FAILED, "Invalid signature"),
    INVALID_ADDRESS(
        Status.FAILED, "Sender address not consistent with public key"),

    // HTTP method result
    HTTP_METHOD_NOT_ALLOWED(
        Status.ERROR, "Method Not Allowed"),
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

    public String toMessageJson() {
        return StringUtil.singleEntryJson("message", getMessage());
    }
}
