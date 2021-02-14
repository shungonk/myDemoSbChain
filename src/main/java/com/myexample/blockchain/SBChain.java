package com.myexample.blockchain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.gson.GsonBuilder;

public class SBChain {

    public static int difficulty = 4;
    public static float minimumTransactionValue = 0.1f;

    public static UTXOPool uTXOPool = new UTXOPool();

    private static List<Block> chain = new ArrayList<>();
	private static List<Transaction> transactionPool = Collections.synchronizedList(new ArrayList<>());

    private SBChain() {}

    public static String marshalJson() {
        return new GsonBuilder()
            .setPrettyPrinting()
            .create().toJson(chain);		
    }

    public static boolean addTransaction(Transaction transaction) {
        synchronized (transactionPool) {
            if (Objects.isNull(transaction)) return false;
            if (!transaction.doProcess()) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
            transactionPool.add(transaction);
            System.out.println("Transaction Successfully pooled");
            return true;
        }
    }

    public static void mining() {
        synchronized (chain) {
            var transactions = List.copyOf(transactionPool);
            var newBlock = chain.isEmpty()
                ? new Block("0", transactions)
                : new Block(chain.get(chain.size() - 1).getHash(), transactions);
            newBlock.proofOfWork(difficulty);
            chain.add(newBlock);
            transactionPool.removeAll(transactions);
            System.out.println(newBlock.marshalJson()); //debug
        }
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
