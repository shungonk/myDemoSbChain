package com.myexample.common;

import com.myexample.common.utils.StringUtil;

public enum Result {

    // Purchase message
    PURCHASE_SUCCESS(
        Status.SUCCESS, "Purchase successed!"),

    // Transaction message
    TRANSACTION_MISSING_FIELDS(
        Status.FAILED, "Transaction missing field(s)"),
    TRANSACTION_INVALID_SIGNATURE(
        Status.FAILED, "Signature is invalid"),
    TRANSACTION_NOTENOUGH_BALANCE(
        Status.FAILED, "Not enough balance"),
    TRANSACTION_TOOSMALL_INPUTS(
        Status.FAILED, "Transaction inputs too small"),
    TRANSACTION_SUCCESS(
        Status.SUCCESS, "Transaction successfully accepted!"),

    // Mining message
    MINING_NOT_MINER(
        Status.FAILED, "This wallet has no rights to mine"),
    MINING_POOL_EMPTY(
        Status.FAILED, "Transaction pool is empty"),
    MINING_SUCCESS(
        Status.SUCCESS, "Mining Completed!"),

    // HTTP method message
    HTTP_METHOD_NOT_ALLOWED(
        Status.ERROR, "Error: Method Not Allowed"),
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
