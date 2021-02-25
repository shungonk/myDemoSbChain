package com.myexample.blockchain;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.myexample.common.constant.Result;
import com.myexample.common.utils.FileUtil;
import com.myexample.common.utils.PropertyUtil;
import com.myexample.common.utils.StringUtil;
import com.myexample.request.TransactionRequest;

public class SBChain {

    public static final int DIFFICULTY = 5;
    public static final float MINIMUM_TRANSACTION_VALUE = 0.00000001f;
    public static final float MINING_REWARD = 10f;
    public static final String BLOCKCHAIN_NAME = "THE SBCHAIN";
    public static final String MINER_ADDRESS = PropertyUtil.getProperty("mineraddress");

    public static UTXOPool uTXOPool = new UTXOPool();
    private static List<Block> chain = new ArrayList<>(Arrays.asList(Block.INITIAL));
	private static List<Transaction> transactionPool = Collections.synchronizedList(new ArrayList<>());

    static {
        try {
            loadChain();
        } catch (NoSuchFileException e) {
            System.out.println("Chain file not found");
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            loadTransactionPool();
        } catch (NoSuchFileException e) {
            System.out.println("TransactionPool file not found");
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        loadUTXOPool();
    }

    private SBChain() {}

    private static Block lastBlock() {
        return chain.get(chain.size() - 1);
    }

    public static int getChainSize() {
        return chain.size();
    }

    public static boolean isTransactionPoolEmpty() {
        return transactionPool.isEmpty();
    }

    public static String marshalJson() {
        return StringUtil.toJson(chain);
    }

    public static String marshalJsonPrettyPrinting() {
        var gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        return gsonBuilder.toJson(chain);
    }

    public static String transactionPoolJson() {
        return StringUtil.toJson(transactionPool);
    }

    public static String transactionPoolJsonPrettyPrinting() {
        var gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        return gsonBuilder.toJson(transactionPool);
    }

    public static Result addGenesisTransaction(String recipientAddress, float value) {
        synchronized (transactionPool) {
            var transaction = new Transaction(
                BLOCKCHAIN_NAME, 
                recipientAddress,
                value);
            transaction.processGenesisTransaction();
            transactionPool.add(transaction);
            System.out.println("Genesis transaction successfully pooled.");
            System.out.println(transaction.marshalJsonPrettyPrinting());

            // save objects
            saveTransactionPool();
            return Result.TRANSACTION_SUCCESS;
        }
    }
    
    public static Result acceptTransactionRequest(TransactionRequest request) {
        synchronized (transactionPool) {
            var transaction = new Transaction(
                request.getSenderAddress(),
                request.getRecipientAddress(),
                request.getValue());
            Result result = transaction.processTransaction();
            if (!result.isSuccess()) {
                return result;
            }
            transactionPool.add(transaction);
            System.out.println("Requested transaction successfully pooled.");
            System.out.println(transaction.marshalJsonPrettyPrinting());

            // save objects
            saveTransactionPool();
            return result;
        }
    }

    public static Result mining() {
        synchronized (chain) {
            if (transactionPool.isEmpty())
                return Result.MINING_POOL_EMPTY;

            // send reward to miner
            addGenesisTransaction(MINER_ADDRESS, MINING_REWARD);

            System.out.println("Mining...");
            var transactions = List.copyOf(transactionPool);
            var newBlock = new Block(lastBlock().getHash(), transactions);
            newBlock.proofOfWork(DIFFICULTY);
            chain.add(newBlock);
            transactionPool.removeAll(transactions);
            
		    System.out.println("========== Block Mined!!! ==========");
            System.out.println(newBlock.marshalJsonPrettyPrinting());

            // save objects
            saveChain();
            saveTransactionPool();
            return Result.MINING_SUCCESS;
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

    private static void saveChain() {
        try {
            var path = PropertyUtil.getProperty("chainfile");
            FileUtil.serializeObject(path, chain);
            System.out.println("Blockchain is saved");
        } catch (IOException e) {
            System.out.println("Failed to save chain");
            e.printStackTrace();
        }
    }

    private static void saveTransactionPool() {
        try {
            var path = PropertyUtil.getProperty("transactionsfile");
            FileUtil.serializeObject(path, transactionPool);
            System.out.println("Transaction pool is saved");
        } catch (IOException e) {
            System.out.println("Failed to save transaction pool");
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadChain() throws IOException, ClassNotFoundException {
        var path = PropertyUtil.getProperty("chainfile");
        chain = FileUtil.deserializeObject(path, chain.getClass());
    }

    @SuppressWarnings("unchecked")
    private static void loadTransactionPool() throws IOException, ClassNotFoundException {
        var path = PropertyUtil.getProperty("transactionsfile");
        transactionPool = FileUtil.deserializeObject(path, transactionPool.getClass());
    }

    private static void loadUTXOPool() {
        for (var block: chain) {
            var transactions = block.getTransactions();
            for (var t: transactions) {
                uTXOPool.putAll(t.getOutputs());
                uTXOPool.removeAll(t.getInputs());
            }
        }
        for (var t: transactionPool) {
            uTXOPool.putAll(t.getOutputs());
            uTXOPool.removeAll(t.getInputs());
        }
    }
}
