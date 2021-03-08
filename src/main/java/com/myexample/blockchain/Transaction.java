package com.myexample.blockchain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.time.Instant;

import com.myexample.common.LogWriter;
import com.myexample.util.SecurityUtil;
import com.myexample.util.StringUtil;

/**
 * 取引データクラス
 * 
 * @author  S.Nakanishi
 * @version 1.0
 * @see     Block
 */
public class Transaction implements Serializable {

    private static final long serialVersionUID = -4830434376199056194L;

    /**
     * インスタンスを一意に特定するハッシュ値です。このフィールドはインスタンス生成時に
     * {@link #calculateHash()}の値が設定されます。
     */
    private String transactionId;

    /**
     * インスタンスが生成された時刻を表します。このフィールドはインスタンス生成時に設定されます。
     */
    private long timestamp;

    /**
     * 取引の送金元アドレスです。このフィールドはインスタンス生成時に設定されます。
     */
    private String senderAddress;

    /**
     * 取引の送金先アドレスです。このフィールドはインスタンス生成時に設定されます。
     */
    private String recipientAddress;

    /**
     * 取引金額です。このフィールドはインスタンス生成時に設定されます。
     */
    private BigDecimal amount;

    /**
     * 取引の認証に使用されたシグネチャです。このフィールドはインスタンス生成時に設定されます。
     * このオブジェクトに格納しブロックチェーンに保管することで、取引のリクエストが再利用される
     * ことを防止します。
     */
    private String signature;

    /**
     * コンストラクタ
     * 
     * <p>取引の送金元アドレス、取引の送金先アドレス、取引金額、取引の認証に使用されたシグネチャ
     * をフィールドに設定します。
     * 
     * @param senderAddress
     *        取引の送金元アドレス
     * @param recipientAddress
     *        取引の送金先アドレス
     * @param amount
     *        取引金額
     * @param signature
     *        取引の認証に使用されたシグネチャ
     */
    public Transaction(String senderAddress, String recipientAddress,
            BigDecimal amount, String signature) {
        this.timestamp = Instant.now().toEpochMilli();
        this.senderAddress = senderAddress;
        this.recipientAddress = recipientAddress;
        this.amount = amount.setScale(SBChain.TRANSACTION_AMOUNT_SCALE);
        this.signature = signature;
        this.transactionId = calculateHash();
    }

    /**
     * 取引データのIDを取得します。
     * 
     * @return 取引データID
     */
    public String getTransactionId() {
        return transactionId;
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
     * 取引の送金元アドレスを取得します。
     * 
     * @return 取引の送金元アドレス
     */
    public String getSenderAddress() {
        return senderAddress;
    }

    /**
     * 取引の送金先アドレスを取得します。
     * 
     * @return 取引の送金先アドレス
     */
    public String getRecipientAddress() {
        return recipientAddress;
    }

    /**
     * 取引金額を取得します。
     * 
     * @return 取引金額
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * 取引の認証に使用されたシグネチャを取得します。
     * 
     * @return 取引の認証に使用されたシグネチャ
     */
    public String getSignature() {
        return signature;
    }

    /**
     * 取引データのハッシュ値を計算します。計算には取引データの全ての情報を使用します。この
     * ハッシュ値は取引データのIDとして使用します。
     * 
     * @return ハッシュ値
     * 
     * @throws RuntimeException
     *         ハッシュ値の計算でセキュリティエラーが発生した場合
     */
    public String calculateHash() {
        try {
            return SecurityUtil.sha256(Long.toString(timestamp) + senderAddress
                 + recipientAddress + amount.toPlainString() + signature);
        } catch (GeneralSecurityException e) {
            LogWriter.severe("Falied to apply hash Algorithm");
            throw new RuntimeException(e);
        }
    }
    
    /**
     * インスタンスのJsonを取得します。
     * 
     * @return Json文字列
     */
    public String toJson() {
        return StringUtil.toJson(this);
    }

    /**
     * インスタンスの整形Jsonを取得します。
     * 
     * @return 整形Json文字列
     */
    public String toJsonPrettyPrinting() {
        return StringUtil.toJsonPrettyPrinting(this);
    }
}
