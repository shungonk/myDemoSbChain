package com.myexample.blockchain;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.myexample.transaction.UTXOPool;

public class SBChain {

    public static int difficulty = 4;
    public static float minimumTransactionValue = 0.1f;

    public static UTXOPool uTXOPool = new UTXOPool();
    private static List<Block> chain = new ArrayList<>(Arrays.asList(Block.createInitial()));

    public static String marshalJson() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(chain);		
    }

    public static List<Block> unmarshalJson(String json) {
        Type chainType = new TypeToken<List<Block>>(){}.getType();
        return new GsonBuilder().setPrettyPrinting().create().fromJson(json, chainType);
    }

    public static Block lastBlock() {
        return chain.get(chain.size() - 1);
    }

    public static void addNewBlock(Block block) {
        block.mining(difficulty);
        chain.add(block);
    }

    public static boolean isChainValid() {
        Block previousBlock, currentBlock;
        for (int i = 1; i < chain.size(); i++) {
            previousBlock = chain.get(i - 1);
            currentBlock = chain.get(i);
            if (!previousBlock.getHash().equals(currentBlock.getPreviousHash())
                || !currentBlock.getHash().equals(currentBlock.calculateHash())) {
                return false;
            }
        }
        return true;
    }
}
