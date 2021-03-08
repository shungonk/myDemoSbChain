package com.myexample.request;

import java.math.BigDecimal;
import java.security.GeneralSecurityException;

import com.myexample.common.LogWriter;
import com.myexample.util.SecurityUtil;

/**
 * 通貨送金のリクエストクラス
 * 
 * <p>{@link SignatureCertifier}抽象クラスを継承します。
 * 
 * @author  S.Nakanishi
 * @version 1.0
 * @see     SignatureCertifier
 */
public class TransactionRequest extends SignatureCertifier {

    private String senderAddress;
    private String recipientAddress;
    private BigDecimal amount;
    private long timestamp;

    /**
     * コンストラクタ
     * 
     * @param senderAddress
     *        送金元アドレス
     * @param recipientAddress
     *        送金先アドレス
     * @param amount
     *        送金金額
     * @param timestamp
     *        時刻（エポック秒）
     */
    public TransactionRequest(String senderAddress, String recipientAddress,
            BigDecimal amount, long timestamp) {
        this.senderAddress = senderAddress;
        this.recipientAddress = recipientAddress;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    /**
     * コンストラクタで設定した送金元アドレスを取得します。
     * 
     * @return 送金元アドレス
     */
    public String getSenderAddress() {
        return senderAddress;
    }

    /**
     * コンストラクタで設定した送金先アドレスを取得します。
     * 
     * @return 送金先アドレス
     */
    public String getRecipientAddress() {
        return recipientAddress;
    }

    /**
     * コンストラクタで設定した送金金額を取得します。
     * 
     * @return 送金金額
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * コンストラクタで設定した時刻（エポック秒）を取得します。
     * 
     * @return 時刻（エポック秒）
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * コンストラクタで設定した送金元アドレス、送金先アドレス、送金金額、時刻（エポック秒）、
     * を連結した文字列からバイト配列を取得します。
     */
    @Override
    public byte[] getData() {
        String data = senderAddress + recipientAddress
             + amount.toPlainString() + Long.toString(timestamp);
        return data.getBytes();
    }

    @Override
    public boolean validateFields() {
        if (publicKey == null || publicKey.isBlank() ||
            senderAddress == null || senderAddress.isBlank() ||
            recipientAddress == null || recipientAddress.isBlank() ||
            amount == null ||
            signature == null || signature.isBlank() ||
            timestamp == 0) {
            return false;
        }
        return true;
    }

    /**
     * 送金金額が正の値であるかどうかを検証します。
     * 
     * @return {@code true} 送金金額が０より大きい場合
     */
    public boolean validateAmount() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 送金元アドレスを検証します。
     * 
     * @return {@code true} 送金元アドレスがパブリックキー
     * （{@link SignatureCertifier#publicKey}）から生成されたものである場合
     * @see    SecurityUtil#verifyAddressByPublicKey(String,String)
     */
    public final boolean verifyAddress() {
        try {
            return SecurityUtil.verifyAddressByPublicKey(
                senderAddress, publicKey);
        } catch (GeneralSecurityException e) {
            LogWriter.warning("Falied to verify address by public key");
            return false;
        }
    }

    /**
     * 送金先アドレスがBase58エンコード表現の文字列であるかどうかを検証します。
     * 
     * @return {@code true} 送金先アドレスがBase58エンコードされた文字列である場合
     */
    public boolean isRecipientAddressBase58() {
		return recipientAddress.matches(
            "[123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz]*");
    }
}
