package com.myexample.blockchain;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.myexample.common.LogWriter;
import com.myexample.common.SecurityUtil;
import com.myexample.common.StringUtil;

public class Block implements Serializable {

    private static final long serialVersionUID = 5762484348074109752L;

    private long timestamp;
    private String previousHash;
    private List<Transaction> transactions;
    private String merkleRoot;
    private int nonce;

    public static final Block INITIAL = new Block("0", new ArrayList<>());

    public Block(String previousHash, List<Transaction> transactions) {
        this.timestamp = Instant.now().toEpochMilli();
        this.previousHash = previousHash;
        this.transactions = transactions;
        this.merkleRoot = calculateMerkleTree();
    }

    public long getTimestamp() {
        return timestamp;
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

    public int getNonce() {
        return nonce;
    }

    public String toJson() {
        return StringUtil.toJson(this);
    }

    public String toJsonPrettyPrinting() {
        return StringUtil.toJsonPrettyPrinting(this);
    }

    public String calculateHash() {
        try {
            return SecurityUtil.sha256(
                Long.toString(timestamp) + previousHash + Integer.toString(nonce) + merkleRoot);
        } catch (GeneralSecurityException e) {
            LogWriter.severe("Falied to apply hash Algorithm");
            throw new RuntimeException(e);
        }
    }

    public String calculateMerkleTree() {
        var treeLayer = transactions.stream()
            .map(Transaction::getTransactionId)
            .collect(Collectors.toList());
        try {
            while (treeLayer.size() > 1) {
                var nextTreeLayer = new ArrayList<String>(); 
                for (int i = 1; i < treeLayer.size(); i++) {
                    nextTreeLayer.add(SecurityUtil.sha256(treeLayer.get(i - 1) + treeLayer.get(i)));
                }
                treeLayer = nextTreeLayer;
            }
            return treeLayer.size() == 1 ? treeLayer.get(0) : ""; 
        } catch (GeneralSecurityException e) {
            LogWriter.severe("Falied to apply hash Algorithm");
            throw new RuntimeException(e);
        }
    }

    public void proofOfWork(int difficulty) {
        var zeros = StringUtil.repeat("0", difficulty);
        String hash;
        do {
            nonce++;
            hash = calculateHash();
        } while (!hash.substring(0, difficulty).equals(zeros));
    }
}