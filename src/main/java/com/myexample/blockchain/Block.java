package com.myexample.blockchain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.GsonBuilder;
import com.myexample.transaction.Transaction;
import com.myexample.utils.CryptoUtil;
import com.myexample.utils.StringUtil;

public class Block {

    private String hash;
    private String previousHash;
    private String merkleRoot;
	private List<Transaction> transactions = new ArrayList<>();
    private long timestamp;
    private int nonce;

    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timestamp = Instant.now().toEpochMilli();
        this.hash = calculateHash();
    }

    public static Block createInitial() {
        return new Block("0");
    }

    public String getHash() {
        return hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getMerkleRoot() {
        return merkleRoot;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getNonce() {
        return nonce;
    }

    public String calculateHash() {
        return CryptoUtil.sha256(
            previousHash + Long.toString(timestamp) + Integer.toString(nonce) + merkleRoot);
    }

    public String marshalJson() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

    public static Block unmarshalJson(String json) {
        return new GsonBuilder().setPrettyPrinting().create().fromJson(json, Block.class);
    }

    public boolean addTransaction(Transaction transaction) {
        if (Objects.isNull(transaction)) return false;

        if (transaction.doProcess()) {
            transactions.add(transaction);
            System.out.println("Transaction Successfully added to Block");
            return true;
        } 
        System.out.println("Transaction failed to process. Discarded.");
        return false;
    }

    public void mining(int difficulty) {
        merkleRoot = StringUtil.merkleRoot(transactions);
        String zeros = StringUtil.repeat("0", difficulty);
        while (!hash.substring(0, difficulty).equals(zeros)) {
            nonce++;
            hash = calculateHash();
        }
		System.out.println("Block Mined!!! : " + hash);
    }
}