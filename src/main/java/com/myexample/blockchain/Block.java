package com.myexample.blockchain;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.myexample.common.LogWriter;
import com.myexample.util.SecurityUtil;
import com.myexample.util.StringUtil;

/**
 * ブロックチェーンに格納するブロッククラス
 * 
 * @author  S.Nakanishi
 * @version 1.0
 * @see     SBChain
 * @see     Transaction
 */
public class Block implements Serializable {

    private static final long serialVersionUID = 5762484348074109752L;

    /**
     * インスタンスが生成された時刻（エポック秒）です。このフィールドはインスタンス生成時
     * に設定されます。
     */
    private long timestamp;

    /**
     * ブロックチェーンに格納されている一つ前のブロックのハッシュ値を設定します。この
     * フィールドはインスタンス生成時に設定されます。
     * 
     * @see #calculateHash()
     */
    private String previousHash;

    /**
     * ブロックに保管する取引データリストを格納します。このフィールドはインスタンス生成時
     * に設定されま。
     * 
     * @see Transaction
     */
    private List<Transaction> transactions;

    /**
     * {@code transactions}を元に生成されるハッシュ値です。{@code Block}のハッシュ値
     * を求める計算で{@code transactions}の代わりに使用されます。このフィールドは
     * インスタンス生成時に設定されます。
     * 
     * @see #calculateMerkleTree()
     * @see #calculateHash()
     */
    private String merkleRoot;

    /**
     * {@link #proofOfWork(int)}を実行することで算出されるナンスです。この値はブロック
     * チェーンに保管するデータそのものではなく、ブロックチェーンのセキュリティのための値です。
     * {@link #proofOfWork(int)}が実行されるまでこのフィールドは初期化されません。
     * 
     * @see #proofOfWork(int)
     */
    private int nonce;

    /**
     * ブロックチェーンの一つ目のブロックインスタンスです。
     */
    public static final Block INITIAL = new Block("0", new ArrayList<>());

    /**
     * コンストラクタ
     * 
     * <p>ブロックチェーンに格納される一つ前のブロックのハッシュ値、取引データリストを
     * フィールドに設定します。
     * 
     * @param  previousHash
     *         ブロックチェーンに格納される一つ前のブロックのハッシュ値
     * @param  transactions
     *         取引データリスト
     * 
     * @see #calculateMerkleTree()
     */
    public Block(String previousHash, List<Transaction> transactions) {
        this.timestamp = Instant.now().toEpochMilli();
        this.previousHash = previousHash;
        this.transactions = transactions;
        this.merkleRoot = calculateMerkleTree();
    }

    /**
     * インスタンスが生成された時刻（エポック秒）を取得します。
     * 
     * @return インスタンスが生成された時刻（エポック秒）
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 前ブロックのハッシュ値を取得します。
     * 
     * @return 前ブロックのハッシュ値
     */
    public String getPreviousHash() {
        return previousHash;
    }

    /**
     * 取引データリストのハッシュ値を取得します。
     * 
     * @return 取引データリストのハッシュ値
     */
    public String getMerkleRoot() {
        return merkleRoot;
    }

    /**
     * 取引データの不可変リストを取得します。
     * 
     * @return 取引データの不可変リスト
     * 
     * @see    Collections#unmodifiableList(List)
     */
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    /**
     * ナンスを取得します。このフィールドは{@link #proofOfWork(int)}が実行されるまで
     * 初期化されません。
     * 
     * @return ナンス
     * 
     * @see    #proofOfWork(int)
     */
    public int getNonce() {
        return nonce;
    }

    /**
     * インスタンスのJsonを取得します。
     * 
     * @return Json文字列
     * 
     * @see    StringUtil#toJson(Object)
     */
    public String toJson() {
        return StringUtil.toJson(this);
    }

    /**
     * インスタンスの整形Jsonを取得します。
     * 
     * @return 整形Json文字列
     * 
     * @see    StringUtil#toJsonPrettyPrinting(Object)
     */
    public String toJsonPrettyPrinting() {
        return StringUtil.toJsonPrettyPrinting(this);
    }

    /**
     * ブロックのハッシュ値を計算します。計算には時刻（エポック秒）、前ブロックのハッシュ値、
     * ナンス、取引データリストのハッシュ値を連結した文字列を使用します。
     * 
     * @return ハッシュ値
     * @throws RuntimeException
     *         ハッシュ値の計算でセキュリティエラーが発生した場合
     * @see    SecurityUtil#sha256(String)
     */
    public String calculateHash() {
        try {
            return SecurityUtil.sha256(Long.toString(timestamp) + previousHash
                 + Integer.toString(nonce) + merkleRoot);
        } catch (GeneralSecurityException e) {
            LogWriter.severe("Falied to apply hash Algorithm");
            throw new RuntimeException(e);
        }
    }

    /**
     * 取引データリストの各IDを元にハッシュ値を計算します。
     * 
     * @return 取引データリストが空でない場合はハッシュ値、空である場合はブランク
     * 
     * @throws RuntimeException
     *         ハッシュ値の計算でセキュリティエラーが発生した場合
     * @see    Transaction#getTransactionId()
     * @see    SecurityUtil#sha256(String)
     */
    public String calculateMerkleTree() {
        var layer = transactions.stream()
            .map(Transaction::getTransactionId)
            .collect(Collectors.toList());
        try {
            while (layer.size() > 1) {
                var nextLayer = new ArrayList<String>(); 
                for (int i = 1; i < layer.size(); i++) {
                    var h = SecurityUtil.sha256(layer.get(i-1) + layer.get(i));
                    nextLayer.add(h);
                }
                layer = nextLayer;
            }
            return layer.size() == 1 ? layer.get(0) : ""; 
        } catch (GeneralSecurityException e) {
            LogWriter.severe("Falied to apply hash Algorithm");
            throw new RuntimeException(e);
        }
    }

    /**
     * ブロックのハッシュ値の先頭{@code n}桁が０になるナンスを算出します。
     * 
     * @param  n
     *         最終的なブロックのハッシュ値の先頭の０の桁数
     * 
     * @see    #calculateHash()
     */
    public void proofOfWork(int n) {
        var zeros = StringUtil.repeat("0", n);
        String hash;
        do {
            nonce++;
            hash = calculateHash();
        } while (!hash.substring(0, n).equals(zeros));
    }
}