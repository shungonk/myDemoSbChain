package com.myexample.blockchain;

import java.time.Instant;

import com.google.gson.GsonBuilder;
import com.myexample.utils.CryptoUtil;
import com.myexample.utils.StringUtil;

public class Block {

    private String hash;
    private String previousHash;
    private String data;
    private long timestamp;
    private int nonce;

    public Block(String previousHash, String data) {
        this.previousHash = previousHash;
        this.data = data;
        this.timestamp = Instant.now().toEpochMilli();
        this.hash = calculateHash();
    }

    public String getHash() {
        return hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getNonce() {
        return nonce;
    }

    public static Block getInitialBlock() {
        return new Block("0", "Initial block");
    }

    public String calculateHash() {
        return CryptoUtil.sha256(
            previousHash + Long.toString(timestamp) + data + Integer.toString(nonce));
    }

    public String marshalJson() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);		
    }

    public static Block unmarshalJson(String json) {
        return new GsonBuilder().setPrettyPrinting().create().fromJson(json, Block.class);
    }

    public void mining(int difficulty) {
        String zeros = StringUtil.repeat("0", difficulty);
        while (!hash.substring(0, difficulty).equals(zeros)) {
            nonce++;
            hash = calculateHash();
        }
    }
}