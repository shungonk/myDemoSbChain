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
import com.myexample.utils.LogWriter;
import com.myexample.utils.Property;
import com.myexample.utils.StringUtil;

public class SBChain {
    // TODO: Synchronize widh Nodes

    public static final int        MINING_DIFFICULTY = 5;
    public static final BigDecimal MINING_REWARD = new BigDecimal("2");
    public static final int        TRANSACTION_AMOUNT_SCALE = 6;
    public static final BigDecimal TRANSACTION_MAX_AMOUNT = new BigDecimal("30");
    public static final String     BLOCKCHAIN_NAME = "THE SBCHAIN";

    private static String  minerAddress;

    private static List<Block> chain = new ArrayList<>(Arrays.asList(Block.INITIAL));
	private static List<Transaction> transactionPool = Collections.synchronizedList(new ArrayList<>());

    private SBChain() {}

    public static String getMinerAddress() {
        return minerAddress;
    }

    public static void setMinerAddress(String address) {
        if (minerAddress == null)
            minerAddress = address;
    }

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
            if (val.stripTrailingZeros().scale() > TRANSACTION_AMOUNT_SCALE)
                return Result.SCALE_OVERFLOW;
            if (val.compareTo(TRANSACTION_MAX_AMOUNT) > 0)
                return Result.TOO_LARGE_AMOUNT;

            transactionPool.add(new Transaction(BLOCKCHAIN_NAME, recipientAdr, val, null));
            // save objects
            saveTransactionPool();
            return Result.TRANSACTION_SUCCESS;
        }
    }
    
    public static Result addTransaction(String recipientAdr, BigDecimal val, String sign) {
        synchronized (transactionPool) {
            if (val.stripTrailingZeros().scale() > TRANSACTION_AMOUNT_SCALE)
                return Result.SCALE_OVERFLOW;
            if (val.compareTo(TRANSACTION_MAX_AMOUNT) > 0)
                return Result.TOO_LARGE_AMOUNT;
            if (duplicateSignature(sign))
                return Result.SIGNATURE_ALREADY_CONSUMED;

            transactionPool.add(new Transaction(BLOCKCHAIN_NAME, recipientAdr, val, sign));
            // save objects
            saveTransactionPool();
            return Result.TRANSACTION_SUCCESS;
        }
    }
    
    public static Result addTransaction(String senderAdr, String recipientAdr, BigDecimal val, String sign) {
        synchronized (transactionPool) {
            if (val.stripTrailingZeros().scale() > TRANSACTION_AMOUNT_SCALE)
                return Result.SCALE_OVERFLOW;
            if (val.compareTo(TRANSACTION_MAX_AMOUNT) > 0)
                return Result.TOO_LARGE_AMOUNT;
            if (val.compareTo(calculateTotalAmount(senderAdr)) > 0)
                return Result.NOT_ENOUGH_BALANCE;
            if (duplicateSignature(sign))
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
            transactionPool.add(new Transaction(BLOCKCHAIN_NAME, minerAddress, MINING_REWARD, null));

            LogWriter.info("Mining...");
            var transactions = List.copyOf(transactionPool);
            var newBlock = new Block(lastBlock().getHash(), transactions);
            newBlock.proofOfWork(MINING_DIFFICULTY);
            chain.add(newBlock);
            transactionPool.removeAll(transactions);
            
		    LogWriter.info("========== Block Mined!!! ==========\n" + newBlock.marshalJsonPrettyPrinting());

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
            if (!previousBlock.getHash().equals(currentBlock.getPreviousHash()))
                return false;
            if (!currentBlock.getHash().equals(currentBlock.calculateHash()))
                return false;
            if (!currentBlock.getHash().startsWith(StringUtil.repeat("0", MINING_DIFFICULTY)))
                return false;
        }
        return true;
    }

    public static BigDecimal calculateTotalAmount(String address) {
        var transactions = getAllTransactions();
        
        var total = BigDecimal.ZERO.setScale(TRANSACTION_AMOUNT_SCALE);
        for (var t: transactions) {
            if (address.equals(t.getRecipientAddress()))
                total = total.add(t.getAmount());
            if (address.equals(t.getSenderAddress()))
                total = total.subtract(t.getAmount());
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
            var path = Property.getProperty("chainfile");
            FileUtil.serializeObject(path, chain);
            LogWriter.info("Blockchain is saved");
        } catch (IOException e) {
            LogWriter.warning("Failed to save chain");
            e.printStackTrace();
        }
    }

    private static void saveTransactionPool() {
        try {
            var path = Property.getProperty("transactionsfile");
            FileUtil.serializeObject(path, transactionPool);
            LogWriter.info("Transaction pool is saved");
        } catch (IOException e) {
            LogWriter.warning("Failed to save transaction pool");
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static void loadChain() {
        var path = Property.getProperty("chainfile");
        try {
            chain = FileUtil.deserializeObject(path, chain.getClass());
            if (!isChainValid()) {
                LogWriter.severe("Loaded Blockchain is inivalid", new RuntimeException());
            }
            LogWriter.info("Blockchain successfully loaded");
        } catch (NoSuchFileException e) {
            LogWriter.warning("Blockchain file not found: " + e.getMessage());
        } catch (IOException | ClassNotFoundException e) {
            LogWriter.severe("Failed to load blockchain file", new RuntimeException(e));
        }
    }

    @SuppressWarnings("unchecked")
    public static void loadTransactionPool() {
        var path = Property.getProperty("transactionsfile");
        try {
            transactionPool = FileUtil.deserializeObject(path, transactionPool.getClass());
            LogWriter.info("TransactionPool successfully loaded");
        } catch (NoSuchFileException e) {
            LogWriter.warning("TransactionPool file not found: " + e.getMessage());
        } catch (IOException | ClassNotFoundException e) {
            LogWriter.severe("Failed to load transaction pool file", new RuntimeException(e));
        }
    }
}
