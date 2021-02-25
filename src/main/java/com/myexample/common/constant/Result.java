package com.myexample.common.constant;

import com.myexample.common.utils.StringUtil;

public enum Result {

    // Purchase result
    PURCHASE_SUCCESS(
        Status.SUCCESS, "Purchase successed!"),

    // Transaction result
    TRANSACTION_NOTENOUGH_BALANCE(
        Status.FAILED, "Not enough balance"),
    TRANSACTION_TOOSMALL_INPUTS(
        Status.FAILED, "Transaction inputs too small"),
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
