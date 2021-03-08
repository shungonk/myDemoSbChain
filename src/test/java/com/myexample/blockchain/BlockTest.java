package com.myexample.blockchain;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import com.myexample.common.StringUtil;

import org.junit.Test;

public class BlockTest {

    @Test
    public void calculateMerkleTree_NotBlank() throws Exception {
        var t = new Transaction("xxx", "yyy", new BigDecimal("1"), "sign");
        var input = new Block("000", Arrays.asList(t));
        var actual = input.calculateMerkleTree();
        var pattern = "[0-9a-f]{64}";
        assertThat(actual.matches(pattern), is(true));
    }

    @Test
    public void calculateMerkleTree_Blank() throws Exception {
        var input = new Block("000", new ArrayList<Transaction>());
        var actual = input.calculateMerkleTree();
        assertThat(actual, is(""));
    }

    @Test
    public void proofOfWork() throws Exception {
        var t = new Transaction("xxx", "yyy", new BigDecimal("1"), "sign");
        var input = new Block("000", Arrays.asList(t));
        var difficulty = 4;
        input.proofOfWork(difficulty);
        var actual = input.calculateHash().startsWith(StringUtil.repeat("0", difficulty));
        assertThat(actual, is(true));
    }
    
}
