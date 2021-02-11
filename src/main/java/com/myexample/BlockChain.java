package com.myexample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.GsonBuilder;

public class BlockChain {

    public static final int DIFFICULTY = 5; 
    
    private ArrayList<Block> chain;

    public BlockChain() {
        this.chain = new ArrayList<>(Arrays.asList(Block.INITIAL_BLOCK));
    }

    public List<Block> getChain() {
        return chain;
    }

    public String marshalJson() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(chain);		
    }

    //TODO: how to unmarshal?
    // public static ArrayList<Block> unmarshalJson(String json) {
    //     return new GsonBuilder().setPrettyPrinting().create().fromJson(json, ArrayList.class);
    // }

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
		//add our blocks to the blockchain ArrayList:
        BlockChain blockchain = new BlockChain();

        blockchain.addNewBlock("Yo im the second block");
        blockchain.addNewBlock("Hey im the third block");
        blockchain.addNewBlock("Hoo im the fourth block");
			
		System.out.println(blockchain.marshalJson());
	}
}
