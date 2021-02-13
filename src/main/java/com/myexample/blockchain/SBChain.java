package com.myexample.blockchain;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.myexample.transaction.Transaction;
import com.myexample.transaction.UTXOPool;

public class SBChain {

    public static int difficulty = 4;
    public static float minimumTransactionValue = 0.1f;

    public static UTXOPool uTXOPool = new UTXOPool();
    private static List<Block> chain = new ArrayList<>();
    private static Block lastBlock = Block.createInitial();

    public static String marshalJson() {
        return new GsonBuilder()
            // .excludeFieldsWithoutExposeAnnotation()
            .setPrettyPrinting()
            .create().toJson(chain);		
    }

    public static boolean addTransaction(Transaction transaction) {
        return lastBlock.addTransaction(transaction);
    }

    public static void mining() {
        //TODO: have to accept transactions in the middle of mining.
        lastBlock.proofOfWork(difficulty);
        chain.add(lastBlock);
        lastBlock = new Block(lastBlock.getHash());
    }

    public static boolean isChainValid() {
        Block previousBlock, currentBlock;
        for (int i = 1; i < chain.size(); i++) {
            previousBlock = chain.get(i - 1);
            currentBlock = chain.get(i);
            if (!previousBlock.getHash().equals(currentBlock.getPreviousHash())
                || !currentBlock.getHash().equals(currentBlock.calculateHash())) {
                return false;
            }
        }
        return true;
    }
}
