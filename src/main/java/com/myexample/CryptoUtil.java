package com.myexample;

import java.security.MessageDigest;

public class CryptoUtil {

	public static String sha256(String input) {		
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");	        
			byte[] hash = digest.digest(input.getBytes("UTF-8"));
			return printHexBinary(hash);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String printHexBinary(byte[] data) {
		StringBuilder sb = new StringBuilder(data.length * 2);
		for (byte b: data) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
}