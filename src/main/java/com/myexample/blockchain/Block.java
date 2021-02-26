package com.myexample.blockchain;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.GsonBuilder;
import com.myexample.utils.SecurityUtil;
import com.myexample.utils.StringUtil;

public class Block implements Serializable {

    private static final long serialVersionUID = 5762484348074109752L;

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
        return Collections.unmodifiableList(transactions);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getNonce() {
        return nonce;
    }

    public String marshalJson() {
        return StringUtil.toJson(this);
    }

    public String marshalJsonPrettyPrinting() {
        var gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        return gsonBuilder.toJson(this);
    }


    public String calculateHash() {
        return SecurityUtil.sha256(
            previousHash + Long.toString(timestamp) + Integer.toString(nonce) + merkleRoot
            );
    }

    private String calculateMerkleTree() {
        var idList = transactions.stream()
            .map(Transaction::getTransactionId)
            .collect(Collectors.toList());
        return SecurityUtil.calculateMerkleRoot(idList);
    }

    public void proofOfWork(int difficulty) {
        merkleRoot = calculateMerkleTree();
        var zeros = StringUtil.repeat("0", difficulty);
        while (!hash.substring(0, difficulty).equals(zeros)) {
            nonce++;
            hash = calculateHash();
        }
    }
}