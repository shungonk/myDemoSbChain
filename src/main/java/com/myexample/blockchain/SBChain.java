package com.myexample.blockchain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.gson.GsonBuilder;
import com.myexample.common.TransactionRequest;

public class SBChain {

    public static final int DIFFICULTY = 4;
    public static final float MINIMUM_TRANSACTION_VALUE = 0.1f;
    public static final float MINING_REWARD = 2f;
    public static final String BLOCKCHAIN_NAME = "THE SBCHAIN";

    public static String minerAddress;

    public static UTXOPool uTXOPool = new UTXOPool();

    private static List<Block> chain = new ArrayList<>(Arrays.asList(Block.INITIAL));
	private static List<Transaction> transactionPool = Collections.synchronizedList(new ArrayList<>());

    private SBChain() {}

    private static Block lastBlock() {
        return chain.get(chain.size() - 1);
    }

    public static int getChainSize() {
        return chain.size();
    }

    public static int getTransactionPoolSize() {
        return transactionPool.size();
    }
    
    public static boolean acceptTransactionRequest(TransactionRequest request) {
        System.out.println("Accept transaction request");
        System.out.println(request.marshalJsonPrettyPrinting());

        if (!request.verifySignature()) {
            System.out.println("# Transaction Signature failed to verify. Transaction discarded.");
            return false;
        }
        var transaction = new Transaction(
            request.calculateHash(),
            request.getSenderAddress(),
            request.getRecipientAddress(),
            request.getValue());
        return addTransaction(transaction);
    }

    public static boolean addTransaction(Transaction transaction) {
        synchronized (transactionPool) {
            if (Objects.isNull(transaction)) return false;
            if (!transaction.doProcess()) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
            transactionPool.add(transaction);
            System.out.println("Transaction Successfully pooled.");
            return true;
        }
    }

    public static void mining() {
        synchronized (chain) {
            // send mining reward to miner
            var miningTransaction = new Transaction(
                Transaction.GENESIS_ID, 
                BLOCKCHAIN_NAME, 
                minerAddress, 
                MINING_REWARD);
            addTransaction(miningTransaction);

            // mine block
            var transactions = List.copyOf(transactionPool);
            var newBlock = new Block(lastBlock().getHash(), transactions);
            newBlock.proofOfWork(DIFFICULTY);
            chain.add(newBlock);
            transactionPool.removeAll(transactions);
            
		    System.out.println("========== Block Mined!!! ==========");
            System.out.println(newBlock.marshalJson());
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

    public static String marshalJson() {
        return new GsonBuilder()
            .setPrettyPrinting()
            .create().toJson(chain);		
    }
}
