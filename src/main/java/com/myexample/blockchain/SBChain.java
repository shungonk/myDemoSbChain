package com.myexample.blockchain;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.myexample.common.FileUtil;
import com.myexample.common.LogWriter;
import com.myexample.common.Property;
import com.myexample.common.StringUtil;

public class SBChain {
    // TODO: Synchronize widh Nodes

    public static final int        MINING_DIFFICULTY        = 5;
    public static final BigDecimal MINING_REWARD            = new BigDecimal("2");
    public static final int        TRANSACTION_AMOUNT_SCALE = 6;
    public static final BigDecimal TRANSACTION_MAX_AMOUNT   = new BigDecimal("30");
    public static final String     BLOCKCHAIN_NAME          = "SBCHAIN";

    private final String minerAddress = Property.getProperty("mineraddress");
    private final Path dataDir = Path.of(Property.getProperty("datadir"));

    private List<Block> chain;
    private List<Transaction> transactionPool;

    public SBChain() {
        createDataDirIfAbsent();
        if ((this.chain = loadChain()) == null) {
            this.chain = new ArrayList<>(Arrays.asList(Block.INITIAL));
        }
        if ((this.transactionPool = loadTransactionPool()) == null) {
            this.transactionPool = Collections.synchronizedList(new ArrayList<>());
        }
        if (isChainValid())
            LogWriter.info("Blockchain is valid.");
        else
            LogWriter.severe("Blockchain is NOT valid.", new RuntimeException());
    }

    public String getMinerAddress() {
        return minerAddress;
    }

    private Block lastBlock() {
        return chain.get(chain.size() - 1);
    }

    public String chainJson() {
        return StringUtil.toJson(chain);
    }

    public String chainJsonPrettyPrinting() {
        return StringUtil.toJsonPrettyPrinting(chain);
    }

    public String transactionPoolJson() {
        return StringUtil.toJson(transactionPool);
    }

    public String transactionPoolJsonPrettyPrinting() {
        return StringUtil.toJsonPrettyPrinting(transactionPool);
    }

    public Result addTransaction(String recipientAdr, BigDecimal val) {
        synchronized (transactionPool) {
            if (val.stripTrailingZeros().scale() > TRANSACTION_AMOUNT_SCALE)
                return Result.AMOUNT_SCALE_OVERFLOW;
            if (val.compareTo(TRANSACTION_MAX_AMOUNT) > 0)
                return Result.TOO_LARGE_AMOUNT;

            transactionPool.add(new Transaction(BLOCKCHAIN_NAME, recipientAdr, val, null));
            // save objects
            saveTransactionPool();
            return Result.TRANSACTION_SUCCESS;
        }
    }
    
    public Result addTransaction(String recipientAdr, BigDecimal val, String sign) {
        synchronized (transactionPool) {
            if (val.stripTrailingZeros().scale() > TRANSACTION_AMOUNT_SCALE)
                return Result.AMOUNT_SCALE_OVERFLOW;
            if (val.compareTo(TRANSACTION_MAX_AMOUNT) > 0)
                return Result.TOO_LARGE_AMOUNT;
            if (isDuplicatedSignature(sign))
                return Result.SIGNATURE_ALREADY_CONSUMED;

            transactionPool.add(new Transaction(BLOCKCHAIN_NAME, recipientAdr, val, sign));
            // save objects
            saveTransactionPool();
            return Result.TRANSACTION_SUCCESS;
        }
    }
    
    public Result addTransaction(String senderAdr, String recipientAdr, BigDecimal val, String sign) {
        synchronized (transactionPool) {
            if (val.stripTrailingZeros().scale() > TRANSACTION_AMOUNT_SCALE)
                return Result.AMOUNT_SCALE_OVERFLOW;
            if (val.compareTo(TRANSACTION_MAX_AMOUNT) > 0)
                return Result.TOO_LARGE_AMOUNT;
            if (val.compareTo(calculateTotalAmount(senderAdr)) > 0)
                return Result.NOT_ENOUGH_BALANCE;
            if (isDuplicatedSignature(sign))
                return Result.SIGNATURE_ALREADY_CONSUMED;

            transactionPool.add(new Transaction(senderAdr, recipientAdr, val, sign));
            // save objects
            saveTransactionPool();
            return Result.TRANSACTION_SUCCESS;
        }
    }

    public void mining() {
        synchronized (chain) {
            if (transactionPool.isEmpty())
                return;

            // send reward to miner
            transactionPool.add(new Transaction(BLOCKCHAIN_NAME, minerAddress, MINING_REWARD, null));

            LogWriter.info("Mining...");
            var transactions = List.copyOf(transactionPool);
            var newBlock = new Block(lastBlock().calculateHash(), transactions);
            newBlock.proofOfWork(MINING_DIFFICULTY);
            chain.add(newBlock);
            transactionPool.removeAll(transactions);
            
            LogWriter.info("========== Block Mined!!! ==========\n" + newBlock.toJsonPrettyPrinting());

            // save objects
            saveChain();
            saveTransactionPool();
        }
    }

    public void scheduleAutoMining(long delay, TimeUnit unit) {
        ScheduledExecutorService miningExecutor = Executors.newScheduledThreadPool(1);
        miningExecutor.scheduleWithFixedDelay(() -> mining(), 0, delay, unit);
    }

    public boolean isChainValid() {
        Block previousBlock, currentBlock;
        for (int i = 1; i < chain.size(); i++) {
            previousBlock = chain.get(i - 1);
            currentBlock = chain.get(i);
            if (!previousBlock.calculateHash().equals(currentBlock.getPreviousHash()))
                return false;
            if (!currentBlock.calculateHash().startsWith(StringUtil.repeat("0", MINING_DIFFICULTY)))
                return false;
        }
        return true;
    }

    public BigDecimal calculateTotalAmount(String address) {
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

    public boolean isDuplicatedSignature(String signature) {
        var transactions = getAllTransactions();
        return transactions.stream()
            .anyMatch(t -> Objects.equals(signature, t.getSignature()));
    }

    private List<Transaction> getAllTransactions() {
        var transactions = chain
            .stream()
            .map(Block::getTransactions)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        transactions.addAll(transactionPool);
        return transactions;
    }

    public void createDataDirIfAbsent() {
        if (!Files.exists(dataDir, LinkOption.NOFOLLOW_LINKS)) {
            try {
                Files.createDirectories(dataDir);
                LogWriter.info("Success to create directory for saving serialized objects - " 
                    + dataDir.toAbsolutePath());
            } catch (IOException e) {
                LogWriter.severe("Failed to create directory for saving serialized objects - " 
                    + dataDir.toAbsolutePath(), new RuntimeException(e));
            }
        }
    }

    private void saveChain() {
        try {
            var path = dataDir.resolve(Property.getProperty("chainfile"));
            FileUtil.serializeObject(path, chain);
            LogWriter.info("Blockchain is saved");
        } catch (IOException e) {
            LogWriter.warning("Failed to save chain");
            e.printStackTrace();
        }
    }

    private void saveTransactionPool() {
        try {
            var path = dataDir.resolve(Property.getProperty("transactionsfile"));
            FileUtil.serializeObject(path, transactionPool);
            LogWriter.info("Transaction pool is saved");
        } catch (IOException e) {
            LogWriter.warning("Failed to save transaction pool");
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Block> loadChain() {
        try {
            var path = dataDir.resolve(Property.getProperty("chainfile"));
            var obj = (List<Block>) FileUtil.deserializeObject(path);
            LogWriter.info("Blockchain successfully loaded");
            return obj;
        } catch (NoSuchFileException e) {
            LogWriter.warning("Blockchain file not found: " + e.getMessage());
            return null;
        } catch (IOException | ClassNotFoundException e) {
            LogWriter.severe("Failed to load blockchain file");
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Transaction> loadTransactionPool() {
        try {
            var path = dataDir.resolve(Property.getProperty("transactionsfile"));
            var obj = (List<Transaction>) FileUtil.deserializeObject(path);
            LogWriter.info("TransactionPool successfully loaded");
            return obj;
        } catch (NoSuchFileException e) {
            LogWriter.warning("TransactionPool file not found: " + e.getMessage());
            return null;
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            LogWriter.severe("Failed to load transaction pool file");
            throw new RuntimeException(e);
        }
    }
}
