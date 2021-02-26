package com.myexample.blockchain;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.GsonBuilder;
import com.myexample.request.TransactionRequest;
import com.myexample.utils.FileUtil;
import com.myexample.utils.PropertyUtil;
import com.myexample.utils.StringUtil;

public class SBChain {

    public static final int DIFFICULTY = 5;
    public static final int VALUE_SCALE = 6;
    public static final BigDecimal MAX_VALUE = new BigDecimal("30");
    public static final BigDecimal MINING_REWARD = new BigDecimal("2");
    public static final String BLOCKCHAIN_NAME = "THE SBCHAIN";
    public static final String MINER_ADDRESS = PropertyUtil.getProperty("mineraddress");

    private static List<Block> chain = new ArrayList<>(Arrays.asList(Block.INITIAL));
	private static List<Transaction> transactionPool = Collections.synchronizedList(new ArrayList<>());

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

    public static Result addTransaction(String senderAddress, String recipientAddress, BigDecimal value) {
        synchronized (transactionPool) {
            transactionPool.add(new Transaction(senderAddress, recipientAddress, value));
            // save objects
            saveTransactionPool();
            return Result.TRANSACTION_SUCCESS;
        }
    }
    
    public static Result acceptTransactionRequest(TransactionRequest request) {
        synchronized (transactionPool) {
            String senderAdd = request.getSenderAddress();
            String recipientAdd = request.getRecipientAddress();
            BigDecimal value = request.getValue();
            String Signature = request.getSignature();

            if (value.stripTrailingZeros().scale() > VALUE_SCALE)
                return Result.SCALE_OVERFLOW;
            else if (value.compareTo(MAX_VALUE) > 0)
                return Result.TOO_LARGE_VALUE;
            else if (value.compareTo(calculateTotalValue(senderAdd)) > 0)
                return Result.NOT_ENOUGH_BALANCE;
            // Validation Complete!

            transactionPool.add(new Transaction(senderAdd, recipientAdd, value));

            // save objects
            saveTransactionPool();
            return Result.TRANSACTION_SUCCESS;
        }
    }

    public static Result mining() {
        synchronized (chain) {
            if (transactionPool.isEmpty())
                return Result.MINING_POOL_EMPTY;

            // send reward to miner
            addTransaction(BLOCKCHAIN_NAME, MINER_ADDRESS, MINING_REWARD);

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

    public static BigDecimal calculateTotalValue(String address) {
        var transactions = chain
            .stream()
            .map(Block::getTransactions)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        transactions.addAll(transactionPool);
        
        var total = BigDecimal.ZERO.setScale(VALUE_SCALE);
        for (var t: transactions) {
            if (address.equals(t.getRecipientAddress()))
                total = total.add(t.getValue());
            if (address.equals(t.getSenderAddress()))
                total = total.subtract(t.getValue());
        }
        return total;
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
    public static void loadChain() {
        var path = PropertyUtil.getProperty("chainfile");
        try {
            chain = FileUtil.deserializeObject(path, chain.getClass());
        } catch (NoSuchFileException e) {
            System.out.println("Chain file not found: " + e.getMessage());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void loadTransactionPool() {
        var path = PropertyUtil.getProperty("transactionsfile");
        try {
            transactionPool = FileUtil.deserializeObject(path, transactionPool.getClass());
        } catch (NoSuchFileException e) {
            System.out.println("TransactionPool file not found: " + e.getMessage());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
