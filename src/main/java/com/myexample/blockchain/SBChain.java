package com.myexample.blockchain;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.gson.GsonBuilder;
import com.myexample.utils.FileUtil;
import com.myexample.utils.PropertyUtil;
import com.myexample.utils.StringUtil;

public class SBChain {
    // TODO: Synchronize widh Nodes

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

    public static Result addTransaction(String recipientAdr, BigDecimal val) {
        synchronized (transactionPool) {
            if (val.stripTrailingZeros().scale() > VALUE_SCALE)
                return Result.SCALE_OVERFLOW;
            else if (val.compareTo(MAX_VALUE) > 0)
                return Result.TOO_LARGE_VALUE;

            transactionPool.add(new Transaction(BLOCKCHAIN_NAME, recipientAdr, val, null));
            // save objects
            saveTransactionPool();
            return Result.TRANSACTION_SUCCESS;
        }
    }
    
    public static Result addTransaction(String recipientAdr, BigDecimal val, String sign) {
        synchronized (transactionPool) {
            if (val.stripTrailingZeros().scale() > VALUE_SCALE)
                return Result.SCALE_OVERFLOW;
            else if (val.compareTo(MAX_VALUE) > 0)
                return Result.TOO_LARGE_VALUE;
            else if (duplicateSignature(sign))
                return Result.SIGNATURE_ALREADY_CONSUMED;

            transactionPool.add(new Transaction(BLOCKCHAIN_NAME, recipientAdr, val, sign));
            // save objects
            saveTransactionPool();
            return Result.TRANSACTION_SUCCESS;
        }
    }
    
    public static Result addTransaction(String senderAdr, String recipientAdr, BigDecimal val, String sign) {
        synchronized (transactionPool) {
            if (val.stripTrailingZeros().scale() > VALUE_SCALE)
                return Result.SCALE_OVERFLOW;
            else if (val.compareTo(MAX_VALUE) > 0)
                return Result.TOO_LARGE_VALUE;
            else if (val.compareTo(calculateTotalValue(senderAdr)) > 0)
                return Result.NOT_ENOUGH_BALANCE;
            else if (duplicateSignature(sign))
                return Result.SIGNATURE_ALREADY_CONSUMED;

            transactionPool.add(new Transaction(senderAdr, recipientAdr, val, sign));
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
            transactionPool.add(new Transaction(BLOCKCHAIN_NAME, MINER_ADDRESS, MINING_REWARD, null));

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
        var transactions = getAllTransactions();
        
        var total = BigDecimal.ZERO.setScale(VALUE_SCALE);
        for (var t: transactions) {
            if (address.equals(t.getRecipientAddress()))
                total = total.add(t.getValue());
            if (address.equals(t.getSenderAddress()))
                total = total.subtract(t.getValue());
        }
        return total;
    }

    public static boolean duplicateSignature(String signature) {
        var transactions = getAllTransactions();
        return transactions.stream()
            .anyMatch(t -> Objects.equals(signature, t.getSignature()));
    }

    private static List<Transaction> getAllTransactions() {
        var transactions = chain
            .stream()
            .map(Block::getTransactions)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        transactions.addAll(transactionPool);
        return transactions;
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
            System.out.println("Blockchain file not found: " + e.getMessage());
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
