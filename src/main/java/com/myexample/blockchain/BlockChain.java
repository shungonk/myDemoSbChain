package com.myexample.blockchain;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class BlockChain {

    public static final int DIFFICULTY = 4; 
    
    private List<Block> chain;

    public BlockChain() {
        this.chain = new ArrayList<>(Arrays.asList(Block.INITIAL_BLOCK));
    }

    public List<Block> getChain() {
        return chain;
    }

    public String marshalJson() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(chain);		
    }

    public static List<Block> unmarshalJson(String json) {
        Type chainType = new TypeToken<List<Block>>(){}.getType();
        return new GsonBuilder().setPrettyPrinting().create().fromJson(json, chainType);
    }

    public Block lastBlock() {
        return chain.get(chain.size() - 1);
    }

    public void addNewBlock(String data) {
        Block newBlock = new Block(lastBlock().getHash(), data);
        newBlock.mining(DIFFICULTY);
        chain.add(newBlock);
    }

    public boolean isChainValid() {
        Block previousBlock, currentBlock;
        for (int i = 1; i < chain.size(); i++) {
            previousBlock = chain.get(i - 1);
            currentBlock = chain.get(i);
            if (!previousBlock.getHash().equals(currentBlock.getPreviousHash())){
                return false;
            }
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                return false;
            }
        }
        return true;
    }

	public static void main(String[] args) {
        BlockChain blockchain = new BlockChain();

        blockchain.addNewBlock("Yo im the second block");
        blockchain.addNewBlock("Hey im the third block");
        blockchain.addNewBlock("Hoo im the fourth block");
			
		System.out.println(blockchain.marshalJson());
	}
}
