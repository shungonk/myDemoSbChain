package com.myexample.utils;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

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

	public static String encodeKey(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}

	public static byte[] ecdsaSign(PrivateKey privateKey, String data) {
		try {
			var dsa = Signature.getInstance("ECDSA", "BC");
			dsa.initSign(privateKey);
			dsa.update(data.getBytes("UTF-8"));
			return dsa.sign();
		} catch (Exception e) {
			throw new RuntimeException();
		}	
	}

	public static boolean verifyEcdsaSign(PublicKey publicKey, String data, byte[] signature) {
		try {
			var dsa = Signature.getInstance("ECDSA", "BC");
			dsa.initVerify(publicKey);
			dsa.update(data.getBytes("UTF-8"));
			return dsa.verify(signature);
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}
}