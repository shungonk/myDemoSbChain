package com.myexample.blockchain;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.myexample.common.FileUtil;
import com.myexample.common.LogWriter;
import com.myexample.common.Property;
import com.myexample.common.StringUtil;

/**
 * ブロックチェーンクラス
 * 
 * @author  S.Nakanishi
 * @version 1.0
 * @see     Block
 * @see     Transaction
 * @see     Result
 */
public class SBChain {

    /**
     * ブロックチェーンの名称です。マイナーへの報酬を送る時、この値が送金元アドレスとして
     * 取引データに設定されます。
     */
    public static final String BLOCKCHAIN_NAME = "SBCHAIN";

    /**
     * マイニングの難易度を表します。この定数は{@link #mining()}（マイニング）で新しい
     * ブロックをブロックチェーンに追加する際に使用され、値が大きいほど処理に時間を要します。
     * 
     * @see #mining()
     */
    public static final int MINING_DIFFICULTY = 5;

    /**
     * マイナーへの報酬金額です。{@link #mining()}（マイニング）実行時に、この金額が
     * をマイナーへ送金されます。
     * 
     * @see #mining()
     */
    public static final BigDecimal MINING_REWARD = new BigDecimal("2");

    /**
     * 取引金額の小数桁スケールです。この値より大きい小数桁スケールの金額を取引することは
     * できません。取引において金額が丸め込まれることはありません。例えばこの定数の値が6で
     * あれば最小単位は0.000001となり、1.0000009の金額を取引することはできません。
     */
    public static final int TRANSACTION_AMOUNT_SCALE = 6;

    /**
     * 取引金額の最大金額です。この値より大きい金額を一度に取引することはできません。
     */
    public static final BigDecimal TRANSACTION_MAX_AMOUNT = new BigDecimal("30");


    /**
     * マイナーのアドレスです。プロパティファイルから値を取得します。
     */
    private String minerAddress = Property.getProperty("mineraddress");

    /**
     * {@code chain}, {@code transactionPool}をシリアライズしてファイルに保管する
     * ためのディレクトリパスを表します。
     * 
     * @see #loadChain()
     * @see #loadTransactionPool()
     * @see #saveChain()
     * @see #saveTransactionPool()
     */
    private Path dataDir = Path.of(Property.getProperty("datadir"));


    /**
     * ブロックを格納するリストです。{@link #mining()}（マイニング）することによって
     * ブロックがこのリストに追加されます。
     * 
     * <p>このリストオブジェクトはスレッドセーフではありません。したがって、リストにブロックを
     * 追加する処理は同期させる必要があります。
     * 
     * @see #loadChain()
     * @see Block
     */
    private final List<Block> chain;

    /**
     * ブロックに未保管の取引データを格納するリストです。{@link #mining()}（マイニング）
     * によって取引データリストをブロックに保管することに成功すると、このリストからその取引
     * データは全て除去されます。
     * 
     * <p>このリストオブジェクトはスレッドセーフです。
     * 
     * @see #loadTransactionPool()
     * @see Transaction
     */
    private final List<Transaction> transactionPool;

    /**
     * コンストラクタ
     * 
     * <p>ファイルからシリアライズされたブロックチェーン、取引データリストをロードします。
     * ロードされたブロックチェーンが{@link #isChainValid()}によって有効でないと判断
     * された場合は、実行時例外が発生し、このコンストラクタの実行は失敗します。
     * 
     * @see #isChainValid()
     */
    public SBChain() {
        createDataDirIfAbsent();
        this.chain = loadChain();
        this.transactionPool = loadTransactionPool();

        if (isChainValid())
            LogWriter.info("Blockchain is valid.");
        else
            LogWriter.severe("Blockchain is NOT valid.", new RuntimeException());
    }

    /**
     * シリアライズファイルを保存するディレクトリを作成します。該当のディレクトリがすでに存在
     * する場合は処理を行いません。
     */
    private void createDataDirIfAbsent() {
        if (!Files.exists(dataDir)) {
            try {
                Files.createDirectories(dataDir);
                LogWriter.info(
                    "Success to create directory for saving serialized objects" 
                    + " - " + dataDir.toAbsolutePath());
            } catch (IOException e) {
                LogWriter.severe(
                    "Failed to create directory for saving serialized objects" 
                    + " - " + dataDir.toAbsolutePath(), new RuntimeException(e));
            }
        }
    }

    /**
     * ファイルからシリアライズされたブロックチェーンをロードし、オブジェクトを取得します。
     * ファイルが存在しない場合、ブロックチェーンの初期値として{@link Block#INITIAL}
     * が格納された{@code ArrayList}を取得します。
     * 
     * @return ブロックチェーン
     * 
     * @see    FileUtil#deserializeObject(Path)
     */
    @SuppressWarnings("unchecked")
    private List<Block> loadChain() {
        try {
            var path = dataDir.resolve(Property.getProperty("chainfile"));
            var obj = (List<Block>) FileUtil.deserializeObject(path);
            LogWriter.info("Blockchain successfully loaded");
            return obj;
        } catch (NoSuchFileException e) {
            LogWriter.warning("Blockchain file not found: " + e.getMessage());
            return new ArrayList<>(Arrays.asList(Block.INITIAL));
        } catch (IOException | ClassNotFoundException e) {
            LogWriter.severe("Failed to load blockchain file");
            throw new RuntimeException(e);
        }
    }

    /**
     * ファイルからシリアライズされた取引データリストをロードし、オブジェクトを取得します。
     * ファイルが存在しない場合、取引データリストの初期値として同期された空の{@code ArrayList}
     * を取得します。
     * 
     * @return 取引データリスト
     * 
     * @see    FileUtil#deserializeObject(Path)
     */
    @SuppressWarnings("unchecked")
    private List<Transaction> loadTransactionPool() {
        try {
            var path = dataDir.resolve(Property.getProperty("transactionsfile"));
            var obj = (List<Transaction>) FileUtil.deserializeObject(path);
            LogWriter.info("TransactionPool successfully loaded");
            return obj;
        } catch (NoSuchFileException e) {
            LogWriter.warning("TransactionPool file not found: " + e.getMessage());
            return Collections.synchronizedList(new ArrayList<>());
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            LogWriter.severe("Failed to load transaction pool file");
            throw new RuntimeException(e);
        }
    }

    /**
     * ブロックチェーンの最後のブロックを取得します。
     * 
     * @return ブロックチェーンの最後のブロック
     */
    private Block lastBlock() {
        return chain.get(chain.size() - 1);
    }

    /**
     * ブロックチェーンのブロック数を取得します。
     * 
     * @return ブロックチェーンのブロック数
     */
    public int getChainSize() {
        return chain.size();
    }

    /**
     * ブロックチェーンに未保管の取引データ数を取得します。
     * 
     * @return ブロックチェーンに未保管の取引データ数
     */
    public int getTransactionPoolSize() {
        return transactionPool.size();
    }

    /**
     * ブロックチェーンのJSON文字列を取得します。
     * 
     * @return ブロックチェーンのJSON文字列
     * 
     * @see    StringUtil#toJson(Object)
     */
    public String chainJson() {
        return StringUtil.toJson(chain);
    }

    /**
     * ブロックチェーンの整形JSON文字列を取得します。
     * 
     * @return ブロックチェーンの整形JSON文字列
     * 
     * @see    StringUtil#toJsonPrettyPrinting(Object)
     */
    public String chainJsonPrettyPrinting() {
        return StringUtil.toJsonPrettyPrinting(chain);
    }

    /**
     * ブロックチェーンに未保管の取引データリストのJSON文字列を取得します。
     * 
     * @return ブロックチェーンに未保管の取引データリストのJSON文字列
     * 
     * @see    StringUtil#toJson(Object)
     */
    public String transactionPoolJson() {
        return StringUtil.toJson(transactionPool);
    }

    /**
     * ブロックチェーンに未保管の取引データリストの整形JSON文字列を取得します。
     * 
     * @return ブロックチェーンに未保管の取引データリストの整形JSON文字列
     * 
     * @see    StringUtil#toJsonPrettyPrinting(Object)
     */
    public String transactionPoolJsonPrettyPrinting() {
        return StringUtil.toJsonPrettyPrinting(transactionPool);
    }

    /**
     * ブロックチェーンから送金先アドレスへの送金に関する取引データを追加します。ただし、
     * 取引データを追加する前に次の検証を行います。
     * <ul>
     * <li>取引金額{@code val}の小数桁スケールが{@link #TRANSACTION_MAX_AMOUNT}より
     * 大きくないか
     * <li>取引金額{@code val}が{@link #TRANSACTION_MAX_AMOUNT}より大きくないか
     * </ul>
     * 検証がパスしなかった場合、取引データは追加されず、対応する{@link Result}が返却され
     * ます。検証がパスし取引データが追加された後、取引データリストのシリアライズファイルを
     * 更新します。
     * 
     * <p>追加される取引データの送金元アドレスには{@link #BLOCKCHAIN_NAME}が設定され、
     * 取引データのシグネチャには{@code null}が設定されます。取引データにシグネチャや
     * 送信元アドレスを設定する場合は、
     * {@link #addTransaction(String, BigDecimal, String)}または
     * {@link #addTransaction(String, String, BigDecimal, String)}を使用する必要
     * があります。
     * 
     * <p>この処理は取引データリストに対して同期されています。
     * 
     * @param  recipientAdr
     *         取引データに設定される送金先アドレス
     * @param  val
     *         取引データに設定される取引金額
     * @return {@link Result}
     * 
     * @see    Result
     * @see    Transaction
     * @see    #addTransaction(String, BigDecimal, String)
     * @see    #addTransaction(String, String, BigDecimal, String)
     */
    public Result addTransaction(String recipientAdr, BigDecimal val) {
        synchronized (transactionPool) {
            if (val.stripTrailingZeros().scale() > TRANSACTION_AMOUNT_SCALE)
                return Result.AMOUNT_SCALE_OVERFLOW;
            if (val.compareTo(TRANSACTION_MAX_AMOUNT) > 0)
                return Result.TOO_LARGE_AMOUNT;

            transactionPool.add(new Transaction(
                BLOCKCHAIN_NAME, recipientAdr, val, null));
            // save objects
            saveTransactionPool();
            return Result.TRANSACTION_SUCCESS;
        }
    }

    /**
     * ブロックチェーンから送金先アドレスへの送金に関する取引データを追加し、シグネチャを取引
     * データに含めます。ただし、取引データを追加する前に次の検証を行います。
     * <ul>
     * <li>取引金額{@code val}の小数桁スケールが{@link #TRANSACTION_MAX_AMOUNT}より
     * 大きくないか
     * <li>取引金額{@code val}が{@link #TRANSACTION_MAX_AMOUNT}より大きくないか
     * <li>シグネチャ{@code sign}が使用済みでないか
     * （{@link #isDuplicatedSignature(String)}参照）
     * </ul>
     * 検証がパスしなかった場合、取引データは追加されず、対応する{@link Result}が返却され
     * ます。検証がパスし取引データが追加された後、取引データリストのシリアライズファイルを
     * 更新します。
     * 
     * <p>追加される取引データの送金元アドレスには{@link #BLOCKCHAIN_NAME}が設定されます。
     * 取引データに送信元アドレスを設定する場合は、
     * {@link #addTransaction(String, String, BigDecimal, String)}を使用する必要
     * があります。
     * 
     * <p>この処理は取引データリストに対して同期されています。
     * 
     * @param  recipientAdr
     *         トランザクションに設定される送金先アドレス
     * @param  val
     *         トランザクションに設定される取引金額
     * @param  sign
     *         トランザクションに設定するシグネチャ
     * @return {@link Result}
     * 
     * @see    Result
     * @see    Transaction
     * @see    #isDuplicatedSignature(String)
     * @see    #addTransaction(String, String, BigDecimal, String)
     */
    public Result addTransaction(String recipientAdr, BigDecimal val,
            String sign) {
        synchronized (transactionPool) {
            if (val.stripTrailingZeros().scale() > TRANSACTION_AMOUNT_SCALE)
                return Result.AMOUNT_SCALE_OVERFLOW;
            if (val.compareTo(TRANSACTION_MAX_AMOUNT) > 0)
                return Result.TOO_LARGE_AMOUNT;
            if (isDuplicatedSignature(sign))
                return Result.SIGNATURE_ALREADY_CONSUMED;

            transactionPool.add(new Transaction(
                BLOCKCHAIN_NAME, recipientAdr, val, sign));
            // save objects
            saveTransactionPool();
            return Result.TRANSACTION_SUCCESS;
        }
    }
    
    /**
     * 送金元アドレスから送金先アドレスへの送金に関する取引データを追加し、シグネチャを取引
     * データに含めます。ただし、取引データを追加する前に次の検証を行います。
     * <ul>
     * <li>取引金額の小数桁スケールが{@link #TRANSACTION_MAX_AMOUNT}より大きくないか
     * <li>取引金額が{@link #TRANSACTION_MAX_AMOUNT}より大きくないか
     * <li>送信元アドレスの残高が取引金額より大きいかどうか
     * （{@link #calculateTotalAmount(String)}を参照）
     * <li>シグネチャが使用済みでないか（{@link #isDuplicatedSignature(String)}参照）
     * </ul>
     * 検証がパスしなかった場合、取引データは追加されず、対応する{@link Result}が返却され
     * ます。検証がパスし取引データが追加された後、取引データリストのシリアライズファイルを
     * 更新します。
     * 
     * <p>この処理は取引データリストに対して同期されています。
     *
     * @param  senderAdr
     *         トランザクションに設定される送金元アドレス
     * @param  recipientAdr
     *         トランザクションに設定される送金先アドレス
     * @param  val
     *         トランザクションに設定される取引金額
     * @param  sign
     *         トランザクションに設定するシグネチャ
     * @return {@link Result}
     * 
     * @see    Result
     * @see    Transaction
     * @see    #calculateTotalAmount(String)
     * @see    #isDuplicatedSignature(String)
     */
    public Result addTransaction(String senderAdr, String recipientAdr,
            BigDecimal val, String sign) {
        synchronized (transactionPool) {
            if (val.stripTrailingZeros().scale() > TRANSACTION_AMOUNT_SCALE)
                return Result.AMOUNT_SCALE_OVERFLOW;
            if (val.compareTo(TRANSACTION_MAX_AMOUNT) > 0)
                return Result.TOO_LARGE_AMOUNT;
            if (val.compareTo(calculateTotalAmount(senderAdr)) > 0)
                return Result.NOT_ENOUGH_BALANCE;
            if (isDuplicatedSignature(sign))
                return Result.SIGNATURE_ALREADY_CONSUMED;

            transactionPool.add(new Transaction(
                senderAdr, recipientAdr, val, sign));
            // save objects
            saveTransactionPool();
            return Result.TRANSACTION_SUCCESS;
        }
    }

    /**
     * マイニングを行います。取引データリストが空の場合は処理を行いません。
     * 
     * <p>{@link Block#proofOfWork(int)}を実行するの直前に、マイナーに
     * {@link #MINING_REWARD}が報酬として送金されます。{@link Block#proofOfWork(int)}
     * 完了後、ブロックはブロックチェーンに追加され、ブロックに格納された取引データは取引データ
     * リストから除去されます。処理が完了すると、ブロックチェーン及び取引データリストのシリアラ
     * イズファイルを更新します。
     */
    public void mining() {
        synchronized (chain) {
            if (transactionPool.isEmpty())
                return;

            if (!isChainValid())
                LogWriter.severe("Blockchain is NOT valid.", new RuntimeException());

            LogWriter.info("Mining...");
            var transactions = new ArrayList<>(transactionPool);
            // send reward to miner
            transactions.add(new Transaction(
                BLOCKCHAIN_NAME, minerAddress, MINING_REWARD, null));
            var newBlock = new Block(lastBlock().calculateHash(), transactions);
            newBlock.proofOfWork(MINING_DIFFICULTY);
            chain.add(newBlock);
            transactionPool.removeAll(transactions);
    
            LogWriter.info("========== Block Mined!!! ==========\n" 
                + newBlock.toJsonPrettyPrinting());

            // save objects
            saveChain();
            saveTransactionPool();
        }
    }

    /**
     * マイニングを定期的に行うスケジュールを設定します。
     * 
     * @param  delay
     *         周期の値
     * @param  unit
     *         {@code delay}の時間単位
     */
    public void scheduleAutoMining(long delay, TimeUnit unit) {
        var miningExecutor = Executors.newScheduledThreadPool(1);
        miningExecutor.scheduleWithFixedDelay(() -> mining(), 0, delay, unit);
    }

    /**
     * ブロックチェーンの同期が解除されるまで処理を停止します。
     */
    public void waitUntilChainUnlocked() {
        synchronized (chain) {return;}
    }

    /**
     * ブロックチェーンが有効であるかを検証します。
     * 
     * @return {@code true} ブロックチェーンが有効である場合
     */
    public boolean isChainValid() {
        Block previousBlock, currentBlock;
        for (int i = 1; i < chain.size(); i++) {
            previousBlock = chain.get(i - 1);
            currentBlock = chain.get(i);
            if (!previousBlock.calculateHash()
                .equals(currentBlock.getPreviousHash()))
                return false;
            if (!currentBlock.calculateHash()
                .startsWith(StringUtil.repeat("0", MINING_DIFFICULTY)))
                return false;
        }
        return true;
    }

    /**
     * 指定されたアドレスの残高を計算します。ブロックチェーンの全ブロックに含まれる取引データ、
     * ブロックチェーンに未保管の取引データリストを対象として計算します。
     * 
     * @param  address
     *         アドレス
     * @return 残高
     */
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

    /**
     * 指定されたシグネチャが過去に使用されたシグネチャと重複しているかどうかを検証します。
     * ブロックチェーンの全ブロックに含まれる取引データ、ブロックチェーンに未保管の取引データ
     * リストを対象として検証します。
     * 
     * @param  signature
     *         検証するシグネチャ
     * @return {@code true} 過去に使用されたシグネチャと重複していない場合
     */
    public boolean isDuplicatedSignature(String signature) {
        return getAllTransactions()
            .stream()
            .anyMatch(t -> Objects.equals(signature, t.getSignature()));
    }

    /**
     * ブロックチェーンの全ブロックに含まれる取引データ、ブロックチェーンに未保管の取引データ
     * リストを１次元のリストとして取得します。
     * 
     * @return 全ての取引データを含むリスト
     */
    public List<Transaction> getAllTransactions() {
        var transactions = chain
            .stream()
            .map(Block::getTransactions)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        transactions.addAll(transactionPool);
        return transactions;
    }

    /**
     * ブロックチェーンをシリアライズして、ファイルに保存します。
     */
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

    /**
     * 取引データリストをシリアライズして、ファイルに保存します。
     */
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
}
