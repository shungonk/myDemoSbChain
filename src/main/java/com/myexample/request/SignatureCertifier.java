package com.myexample.request;

import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;

import com.myexample.common.LogWriter;
import com.myexample.common.SecurityUtil;
import com.myexample.common.StringUtil;

/**
 * シグネチャ認証を行う抽象クラス
 * 
 * @author  S.Nakanishi
 * @version 1.0
 */
public abstract class SignatureCertifier {

    /**
     * {@link PublicKey}のBase64エンコードされた文字列です。シグネチャ認証に使用
     * します。{@link #signate(String, String)}実行時に値が格納されます。
     */
    protected String publicKey;

    /**
     * {@link Signature}のBase64エンコードされた文字列です。
     * {@link #signate(String, String)}実行時に値が格納されます。
     */
    protected String signature;

    protected SignatureCertifier() {}

    /**
     * シグネチャ認証に使用するBase64表現のパブリックキー文字列を取得します。
     * 
     * @return {@link #publicKey}
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * Base64表現のシグネチャ文字列を取得します。
     * 
     * @return {@link #signature}
     */
    public String getSignature() {
        return signature;
    }

    /**
     * シグネチャ生成に使用するバイト配列データを取得します。
     * 
     * @return バイト配列データ
     */
    public abstract byte[] getData();

    /**
     * 全フィールドに値が設定されているかどうかを検証します。
     * 
     * @return {@code true} 全て条件を満たしている場合
     */
    public abstract boolean validateFields();

    /**
     * シグネチャを生成します。
     * 
     * シグネチャ生成に使用するバイト配列データは{@link #getData()}から取得します。生成
     * されたシグネチャと{@code publicKey}パラメータは、{@link #signature}, 
     * {@link #publicKey}フィールドに格納されます（{@link #publicKey}はシグネチャ認証
     * に使用します）。
     * 
     * @param privateKey 
     *        シグネチャ生成に使用するBase64エンコード表現したプライベートキー文字列。
     * @param publicKey
     *        シグネチャ認証に使用するBase64エンコード表現したパブリックキー文字列。
     * @see   #getData()
     * @see   #verifySignature()
     * @see   SecurityUtil#createEcdsaSign(String, byte[])
     */
    public final void signate(String privateKey, String publicKey) {
        try {
            this.publicKey = publicKey;
            this.signature = SecurityUtil.createEcdsaSign(privateKey, getData());
        } catch (GeneralSecurityException e) {
            LogWriter.warning("Failed to create ECDSA signature");
        }
    }

    /**
     * シグネチャを認証します。
     * 
     * 認証には{@link #publicKey}, {@link #getData()}, {@link #signature}を使用します。
     * 
     * @return {@code true} シグネチャが有効である場合
     * 
     * @see   #getData()
     * @see   #signate(String,String)
     * @see   SecurityUtil#verifyEcdsaSign(String, byte[], String)
     */
    public final boolean verifySignature() {
        try {
            return SecurityUtil.verifyEcdsaSign(publicKey, getData(), signature);
        } catch (GeneralSecurityException e) {
            LogWriter.warning("Failed to verity ECDSA signature");
            return false;
        }
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
     * インスタンスの整形されたJsonを取得します。
     * 
     * @return 整形されたJson文字列
     * 
     * @see    StringUtil#toJsonPrettyPrinting(Object)
     */
    public String toJsonPrettyPrinting() {
        return StringUtil.toJsonPrettyPrinting(this);
    }
}