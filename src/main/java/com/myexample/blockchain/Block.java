package com.myexample.blockchain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.GsonBuilder;
import com.myexample.common.utils.CryptoUtil;

public class Block {

    private String hash;                     // determined in proofOfWork method
    private String previousHash;              
	private List<Transaction> transactions;
    private String merkleRoot;
    private long timestamp;
    private int nonce;                       // determined in proofOfWork method

    public static final Block INITIAL;
    static {
        INITIAL = new Block("0", new ArrayList<>());
        // write initialiation if necessarry.
    }

    public Block(String previousHash, List<Transaction> transactions) {
        this.previousHash = previousHash;
        this.timestamp = Instant.now().toEpochMilli();
        this.transactions = transactions;
        this.merkleRoot = calculateMerkleTree();
        this.hash = calculateHash();
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

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getNonce() {
        return nonce;
    }

    public String marshalJson() {
        return new GsonBuilder()
            .setPrettyPrinting()
            .create().toJson(this);		
    }
    public String calculateHash() {
        return CryptoUtil.sha256(
            previousHash + Long.toString(timestamp) + Integer.toString(nonce) + merkleRoot
            );
    }

    private String calculateMerkleTree() {
        var idList = transactions.stream()
            .map(Transaction::getTransactionId)
            .collect(Collectors.toList());
        return CryptoUtil.calculateMerkleRoot(idList);
    }

    public void proofOfWork(int difficulty) {
        var zeros = String.join("", Collections.nCopies(difficulty, "0"));
        while (!hash.substring(0, difficulty).equals(zeros)) {
            nonce++;
            hash = calculateHash();
        }
    }
}