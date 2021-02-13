package com.myexample.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.myexample.transaction.Transaction;

public class StringUtil {
    
    public static String repeat(String s, int n) {
        return String.join("", Collections.nCopies(n, s));
    }

	public static String merkleRoot(List<Transaction> transactions) {
		var treeLayer = transactions.stream()
					.map(Transaction::getTransactionId)
					.collect(Collectors.toList());

		while (treeLayer.size() > 1) {
			var nextTreeLayer = new ArrayList<String>(); 
			for (int i = 1; i < treeLayer.size(); i++) {
				nextTreeLayer.add(CryptoUtil.sha256(treeLayer.get(i - 1) + treeLayer.get(i)));
			}
			treeLayer = nextTreeLayer;
		}
		
		return treeLayer.size() == 1 ? treeLayer.get(0) : ""; 
	}
}
