package com.myexample.utils;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CryptoUtil {

	public static String sha256(String input) {		
		try {
			var digest = MessageDigest.getInstance("SHA-256");	        
			var hash = digest.digest(input.getBytes("UTF-8"));
			var sb = new StringBuilder(hash.length * 2);
			for (var b: hash) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static KeyPair generateKeyPair() {
        try {
            var generator = KeyPairGenerator.getInstance("ECDSA", "BC");
            var random = SecureRandom.getInstance("SHA1PRNG");
            var eSpec = new ECGenParameterSpec("prime192v1");
            generator.initialize(eSpec, random);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException();
        }
	}

	public static String encodeKeyToString(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}

	public static PublicKey decodePublicKey(String publicKeyString) {
        try {
			var keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyString));
			return KeyFactory.getInstance("ECDSA").generatePublic(keySpec);
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

	public static PrivateKey decodePrivateKey(String privateKeyString) {
        try {
			var keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString));
			return KeyFactory.getInstance("ECDSA").generatePrivate(keySpec);
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

	public static String createEcdsaSign(String privateKeyString, String data) {
		try {
			var dsa = Signature.getInstance("ECDSA", "BC");
			dsa.initSign(decodePrivateKey(privateKeyString));
			dsa.update(data.getBytes("UTF-8"));
			var sign = dsa.sign();
			return Base64.getEncoder().encodeToString(sign);
		} catch (Exception e) {
			throw new RuntimeException();
		}	
	}

	public static boolean verifyEcdsaSign(String publicKeyString, String data, String signature) {
		try {
			var dsa = Signature.getInstance("ECDSA", "BC");
			dsa.initVerify(decodePublicKey(publicKeyString));
			dsa.update(data.getBytes("UTF-8"));
			var sign = Base64.getDecoder().decode(signature);
			return dsa.verify(sign);
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

	public static String calculateMerkleRoot(List<String> hashList) {
		var treeLayer = hashList;
		
		while (treeLayer.size() > 1) {
			var nextTreeLayer = new ArrayList<String>(); 
			for (int i = 1; i < treeLayer.size(); i++) {
				nextTreeLayer.add(sha256(treeLayer.get(i - 1) + treeLayer.get(i)));
			}
			treeLayer = nextTreeLayer;
		}
		
		return treeLayer.size() == 1 ? treeLayer.get(0) : ""; 
	}
}