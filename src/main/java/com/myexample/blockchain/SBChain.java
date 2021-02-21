package com.myexample.blockchain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.myexample.common.TransactionRequest;
import com.myexample.common.utils.PropertyUtil;

public class SBChain {

    public static final int DIFFICULTY = 4;
    public static final float MINIMUM_TRANSACTION_VALUE = 0.1f;
    public static final float MINING_REWARD = 2f;
    public static final String BLOCKCHAIN_NAME = "THE SBCHAIN";
    public static final String MINER_ADDRESS = PropertyUtil.getProperty("mineraddress");

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

    public static String marshalJson() {
        return new Gson().toJson(chain);
    }

    public static String marshalJsonPrettyPrinting() {
        var gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        return gsonBuilder.toJson(chain);
    }

    public static String transactionPoolJson() {
        return new Gson().toJson(transactionPool);
    }

    public static String transactionPoolJsonPrettyPrinting() {
        var gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        return gsonBuilder.toJson(transactionPool);
    }

    public static int getTransactionPoolSize() {
        return transactionPool.size();
    }

    public static boolean addTransaction(Transaction transaction) {
        synchronized (transactionPool) {
            if (!transaction.processTransaction()) {
                return false;
            }
            transactionPool.add(transaction);
            System.out.println("Transaction Successfully pooled.");
            System.out.println(transaction.marshalJsonPrettyPrinting());
            return true;
        }
    }

    public static boolean addGenesisTransaction(String recipientAddress, float value) {
        synchronized (transactionPool) {
            var transaction = new Transaction(
                Transaction.GENESIS_ID, 
                BLOCKCHAIN_NAME, 
                recipientAddress, 
                value);
            transaction.processGenesisTransaction();
            transactionPool.add(transaction);
            System.out.println("Genesis transaction pooled.");
            System.out.println(transaction.marshalJsonPrettyPrinting());
            return true;
        }
    }
    
    public static boolean acceptTransactionRequest(TransactionRequest request) {
        System.out.println("Accepting transaction request");
        System.out.println(request.marshalJson());

        if (!request.validateTransactionRequest()) {
            System.out.println("# Transaction missing field(s)");
            return false;
        }

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

    public static void mining() {
        synchronized (chain) {
            // send reward to miner
            addGenesisTransaction(MINER_ADDRESS, MINING_REWARD);

            // mine block
            System.out.println("Mining...");
            var transactions = List.copyOf(transactionPool);
            var newBlock = new Block(lastBlock().getHash(), transactions);
            newBlock.proofOfWork(DIFFICULTY);
            chain.add(newBlock);
            transactionPool.removeAll(transactions);
            
		    System.out.println("========== Block Mined!!! ==========");
            System.out.println(newBlock.marshalJsonPrettyPrinting());
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
