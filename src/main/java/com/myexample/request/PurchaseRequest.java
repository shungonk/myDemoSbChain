package com.myexample.request;

import java.math.BigDecimal;
import java.security.GeneralSecurityException;

import com.myexample.common.LogWriter;
import com.myexample.util.SecurityUtil;

/**
 * 通貨購入のリクエストクラス
 * 
 * <p>{@link SignatureCertifier}抽象クラスを継承します。
 * 
 * @author  S.Nakanishi
 * @version 1.0
 * @see     SignatureCertifier
 */
public class PurchaseRequest extends SignatureCertifier {

    private String address;
    private BigDecimal amount;
    private long timestamp;

    /**
     * コンストラクタ
     * 
     * @param address
     *        購入者アドレス
     * @param amount
     *        購入金額
     * @param timestamp
     *        時刻（エポック秒）
     */
    public PurchaseRequest(String address, BigDecimal amount, long timestamp) {
        this.address = address;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    /**
     * コンストラクタで設定した購入者アドレスを取得します。
     * 
     * @return {@link #address}
     */
    public String getAddress() {
        return address;
    }

    /**
     * コンストラクタで設定した購入金額を取得します。
     * 
     * @return {@link #amount}
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * コンストラクタで設定した時刻（エポック秒）を取得します。
     * 
     * @return {@link #timestamp}
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * コンストラクタで設定した購入者アドレス、購入金額、時刻（エポック秒）を連結した
     * 文字列からバイト配列を取得します。
     */
    @Override
    public byte[] getData() {
        String data = address
             + amount.toPlainString() + Long.toString(timestamp);
        return data.getBytes();
    }

    @Override
    public boolean validateFields() {
        if (publicKey == null || publicKey.isBlank() ||
            address == null || address.isBlank() ||
            amount == null ||
            signature == null || signature.isBlank() ||
            timestamp == 0) {
            return false;
        }
        return true;
    }

    /**
     * 購入金額が正の値であるかどうかを検証します。
     * 
     * @return {@code true} 購入金額が０より大きい場合
     */
    public boolean validateAmount() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 購入者アドレスを検証します。
     * 
     * @return {@code true} 送金元アドレスがパブリックキー
     * （{@link SignatureCertifier#publicKey}）から生成されたものである場合
     * @see    SecurityUtil#verifyAddressByPublicKey(String,String)
     */
    public boolean verifyAddress() {
        try {
            return SecurityUtil.verifyAddressByPublicKey(address, publicKey);
        } catch (GeneralSecurityException e) {
            LogWriter.warning("Falied to verify address by public key");
            return false;
        }
    }
}
