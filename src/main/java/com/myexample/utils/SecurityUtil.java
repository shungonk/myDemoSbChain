package com.myexample.utils;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import org.bitcoinj.core.Base58;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

public class SecurityUtil {

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

	public static String encodeKeyToString(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}

	public static PublicKey decodePublicKey(String publicKeyString) {
        try {
			var keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyString));
			return KeyFactory.getInstance("ECDSA").generatePublic(keySpec);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static PrivateKey decodePrivateKey(String privateKeyString) {
        try {
			var keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString));
			return KeyFactory.getInstance("ECDSA").generatePrivate(keySpec);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String createEcdsaSign(String privateKeyString, byte[] data) {
		try {
			var dsa = Signature.getInstance("ECDSA", "BC");
			dsa.initSign(decodePrivateKey(privateKeyString));
			dsa.update(data);
			var sign = dsa.sign();
			return Base64.getEncoder().encodeToString(sign);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}	
	}

	public static boolean verifyEcdsaSign(String publicKeyString, byte[] data, String signature) {
		try {
			var dsa = Signature.getInstance("ECDSA", "BC");
			dsa.initVerify(decodePublicKey(publicKeyString));
			dsa.update(data);
			var sign = Base64.getDecoder().decode(signature);
			return dsa.verify(sign);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
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

	public static PublicKey getPublicKeyFromPriavateKey(PrivateKey privateKey) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
			ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
		
			BigInteger d = ((ECPrivateKey) privateKey).getD();
			ECPoint Q = ecSpec.getG().multiply(d);
		
			ECPublicKeySpec pubSpec = new ECPublicKeySpec(Q, ecSpec);
			return keyFactory.generatePublic(pubSpec);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String getAddressFromPublicKey(PublicKey publicKey) {
		try {
            // store just the private part of the key since the public key can be derived from the private key.
            // The static method adjustTo64() merely pads the hex string with leading 0s so the total length is 64 characters.
            // var epvt = (ECPrivateKey) pvt;
            // var sepvt = adjustTo64(epvt.getS().toString(16)).toUpperCase();

            // The public part of the key generated above is encoded into a bitcoin address.
            // the ECDSA public key is represented by a point on an elliptical curve.
            // They are concatenated together with “04” at the beginning to represent the public key.
			var epub = (ECPublicKey)publicKey;
			var pt = epub.getW();
			var sx = adjustTo64(pt.getAffineX().toString(16)).toUpperCase();
			var sy = adjustTo64(pt.getAffineY().toString(16)).toUpperCase();
			var bcPub = "04" + sx + sy;

            // perform a SHA-256 digest on the public key, followed by a RIPEMD-160 digest.
            // We use the Bouncy Castle provider for performing the RIPEMD-160 digest since JCE does not implement this algorithm.
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			byte[] s1 = sha.digest(bcPub.getBytes("UTF-8"));
			MessageDigest rmd = MessageDigest.getInstance("RipeMD160", "BC");
			byte[] r1 = rmd.digest(s1);
        
            // add a version byte of 0x00 at the beginning of the hash.
			byte[] r2 = new byte[r1.length + 1];
			r2[0] = 0;
			for (int i = 0 ; i < r1.length ; i++) r2[i+1] = r1[i];
			
            // perform a SHA-256 hash twice on the result above.
			byte[] s2 = sha.digest(r2);
			byte[] s3 = sha.digest(s2);

            // The first 4 bytes of the result of the second hashing is used as the address checksum.
            // It is appended to the RIPEMD160 hash above. This is the 25-byte bitcoin address.
			byte[] a1 = new byte[25];
			for (int i = 0 ; i < r2.length ; i++) a1[i] = r2[i];
			for (int i = 0 ; i < 5 ; i++) a1[20 + i] = s3[i];

            // now use the Base58.encode() method from the bitcoinj library to arrive at the final bitcoin address.
            // This is the address to which the bitcoin should be sent to in a transaction.
			return Base58.encode(a1);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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

	public static boolean validateAddressByPublicKey(String address, String publicKeyString) {
		var publicKey = decodePublicKey(publicKeyString);
		return Objects.equals(address, getAddressFromPublicKey(publicKey));
	}

	public static boolean validateAddressByPublicKey(String address, PublicKey publicKey) {
		return Objects.equals(address, getAddressFromPublicKey(publicKey));
	}
}