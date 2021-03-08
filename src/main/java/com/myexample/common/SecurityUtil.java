package com.myexample.common;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

import org.bitcoinj.core.Base58;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

/**
 * セキュリティユーティリティクラス
 * 
 * @author  S.Nakanishi
 * @version 1.0
 */
public class SecurityUtil {

	private SecurityUtil() {}

	/**
	 * 文字列をSHA256関数でハッシュ化し、16進数で表現した文字列を取得します。
	 * 文字列のデコード方式はUTF-8です。
	 * 
	 * @param  input
	 *         ハッシュ化する文字列
	 * @return ハッシュ値
	 * 
	 * @throws GeneralSecurityException
	 *         セキュリティエラーが発生した場合
	 * @see    MessageDigest
	 */
	public static String sha256(String input) throws GeneralSecurityException {
		var digest = MessageDigest.getInstance("SHA-256");	        
		var hash = digest.digest(input.getBytes());
		var sb = new StringBuilder(hash.length * 2);
		for (var b: hash) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

	/**
	 * 楕円曲線（EC）アルゴリズムの{@link KeyPair}を生成します。
	 * 
	 * @return 楕円曲線（EC）アルゴリズムの{@link KeyPair}
	 * 
	 * @throws GeneralSecurityException
	 *         セキュリティエラーが発生した場合
	 * @see    KeyPairGenerator
	 * @see    ECGenParameterSpec
	 */
	public static KeyPair generateKeyPair() throws GeneralSecurityException {
		var keyGenerator = KeyPairGenerator.getInstance("EC"); 
		var ecGenSpec = new ECGenParameterSpec("secp256k1");
		keyGenerator.initialize(ecGenSpec);
		return keyGenerator.genKeyPair();
	}

	/**
	 * 指定した{@link Key}をBase64エンコード表現した文字列に変換します。
	 * 
	 * @param  key
	 *         文字列化する{@link Key}
	 * @return Base64エンコード表現したキー文字列
	 * 
	 * @see    Base64
	 */
	public static String encodeKeyToString(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}

	/**
	 * Base64エンコード表現したパブリックキー文字列を楕円曲線（EC）アルゴリズムの
	 * {@link PublicKey}に変換します。
	 * 
	 * @param  publicKeyString
	 *         Base64エンコード表現したパブリックキー文字列
	 * @return 楕円曲線（EC）アルゴリズムの{@link PublicKey}
	 * 
	 * @throws GeneralSecurityException
	 *         セキュリティエラーが発生した場合
	 * @see    Base64
	 * @see    X509EncodedKeySpec
	 * @see    KeyFactory
	 */
	public static PublicKey decodePublicKey(String publicKeyString)
			throws GeneralSecurityException {
		var pub = Base64.getDecoder().decode(publicKeyString);
		var keySpec = new X509EncodedKeySpec(pub);
		return KeyFactory.getInstance("ECDSA").generatePublic(keySpec);
	}

	/**
	 * Base64エンコード表現したプライベートキー文字列を楕円曲線（EC）アルゴリズムの
	 * {@link PrivateKey}に変換します。
	 * 
	 * @param  privateKeyString
	 *         Base64エンコード表現したプライベートキー文字列
	 * @return 楕円曲線（EC）アルゴリズムの{@link PrivateKey}
	 * 
	 * @throws GeneralSecurityException
	 *         セキュリティエラーが発生した場合
	 * @see    Base64
	 * @see    PKCS8EncodedKeySpec
	 * @see    KeyFactory
	 */
	public static PrivateKey decodePrivateKey(String privateKeyString) 
			throws GeneralSecurityException {
		var pvt = Base64.getDecoder().decode(privateKeyString);
		var keySpec = new PKCS8EncodedKeySpec(pvt);
		return KeyFactory.getInstance("ECDSA").generatePrivate(keySpec);
	}

	/**
	 * 楕円曲線（EC）アルゴリズムのシグネチャを生成し、Base64エンコード表現したシグネチャ
	 * 文字列を取得します。
	 * 
	 * @param  privateKeyString
	 *         Base64エンコード表現したプライベートキー文字列
	 * @param  data
	 *         シグネチャ生成に使用するバイト配列データ
	 * @return Base64エンコード表現した楕円曲線（EC）アルゴリズムのシグネチャ文字列
	 * 
	 * @throws GeneralSecurityException
	 *         セキュリティエラーが発生した場合
	 * @see    Signature
	 * @see    Base64
	 */
	public static String createEcdsaSign(String privateKeyString, byte[] data)
			throws GeneralSecurityException {
		var dsa = Signature.getInstance("ECDSA", "BC");
		dsa.initSign(decodePrivateKey(privateKeyString));
		dsa.update(data);
		var sign = dsa.sign();
		return Base64.getEncoder().encodeToString(sign);
	}

	/**
	 * 楕円曲線（EC）アルゴリズムのシグネチャを認証します。
	 * 
	 * @param  publicKeyString
	 *         Base64エンコードされたパブリックキー文字列。
	 * @param  data
	 *         シグネチャ生成時に使用したバイト配列データ
	 *         （{@link #createEcdsaSign(String, byte[])}参照）
	 * @param  signature
	 *         Base64エンコード表現した楕円曲線（EC）アルゴリズムのシグネチャ文字列
	 * @return {@code true} シグネチャが有効である場合
	 * 
	 * @throws GeneralSecurityException
	 *         セキュリティエラーが発生した場合
	 * @see    Signature
	 * @see    Base64
	 */
	public static boolean verifyEcdsaSign(String publicKeyString, byte[] data,
			String signature) throws GeneralSecurityException {
		var dsa = Signature.getInstance("ECDSA", "BC");
		dsa.initVerify(decodePublicKey(publicKeyString));
		dsa.update(data);
		var sign = Base64.getDecoder().decode(signature);
		return dsa.verify(sign);
	}

	/**
	 * 楕円曲線（EC）アルゴリズムの{@link PrivateKey}から{@link PublicKey}を生成します。
	 * 
	 * @param  privateKey
	 *         楕円曲線（EC）アルゴリズムの{@link PrivateKey}
	 * @return 楕円曲線（EC）アルゴリズムの{@link PublicKey}
	 * 
	 * @throws GeneralSecurityException
	 *         セキュリティエラーが発生した場合
	 * @see    KeyFactory
	 * @see    ECNamedCurveTable
	 * @see    ECPrivateKey
	 * @see    ECPoint
	 * @see    ECPublicKeySpec
	 */
	public static PublicKey getPublicKeyFromPriavateKey(PrivateKey privateKey)
			throws GeneralSecurityException {
		var keyFactory = KeyFactory.getInstance("ECDSA", "BC");
		var ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
		BigInteger d = ((ECPrivateKey) privateKey).getD();
		ECPoint Q = ecSpec.getG().multiply(d);
		var pubSpec = new ECPublicKeySpec(Q, ecSpec);
		return keyFactory.generatePublic(pubSpec);
	}

	/**
	 * 楕円曲線（EC）アルゴリズムの{@link PublicKey}からBase58エンコード表現した
	 * アドレス文字列を生成します。
	 * 
	 * @param  publicKey
	 *         楕円曲線（EC）アルゴリズムの{@link PublicKey}
	 * @return Base58エンコード表現したアドレス文字列
	 * 
	 * @throws GeneralSecurityException
	 *         セキュリティエラーが発生した場合
	 * @see    ECPublicKey
	 * @see    ECPoint
	 * @see    MessageDigest
	 * @see    Base58
	 */
	public static String getAddressFromPublicKey(PublicKey publicKey)
			throws GeneralSecurityException {

		var epub = (ECPublicKey)publicKey;
		var pt = epub.getW();
		var sx = adjustTo64(pt.getAffineX().toString(16)).toUpperCase();
		var sy = adjustTo64(pt.getAffineY().toString(16)).toUpperCase();
		var bcPub = "04" + sx + sy;

		MessageDigest sha = MessageDigest.getInstance("SHA-256");
		byte[] s1 = sha.digest(bcPub.getBytes());
		MessageDigest rmd = MessageDigest.getInstance("RipeMD160", "BC");
		byte[] r1 = rmd.digest(s1);
	
		byte[] r2 = new byte[r1.length + 1];
		r2[0] = 0;
		for (int i = 0 ; i < r1.length ; i++) r2[i+1] = r1[i];
		
		byte[] s2 = sha.digest(r2);
		byte[] s3 = sha.digest(s2);

		byte[] a1 = new byte[25];
		for (int i = 0 ; i < r2.length ; i++) a1[i] = r2[i];
		for (int i = 0 ; i < 5 ; i++) a1[20 + i] = s3[i];

		return Base58.encode(a1);
	}

    private static String adjustTo64(String s) {
        switch (s.length()) {
            case 62: return "00" + s;
            case 63: return "0" + s;
            case 64: return s;
            default:
                throw new IllegalArgumentException("not a valid key: " + s);
        }
    }

	/**
	 * アドレスをパブリックキーで検証します。
	 * 
	 * @param  address
	 *         Base58エンコード表現したアドレス文字列
	 * @param  publicKeyString
	 *         Base64エンコード表現したパブリックキー文字列
	 * @return {@code true} {@code publicKeyString}から生成されたアドレスが
	 *         {@code address}と等しい場合
	 * @throws GeneralSecurityException
	 *         セキュリティエラーが発生した場合
	 * @see    #getAddressFromPublicKey(PublicKey)
	 */
	public static boolean verifyAddressByPublicKey(String address,
			String publicKeyString) throws GeneralSecurityException {
		var publicKey = decodePublicKey(publicKeyString);
		return Objects.equals(address, getAddressFromPublicKey(publicKey));
	}
}