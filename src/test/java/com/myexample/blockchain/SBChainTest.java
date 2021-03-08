package com.myexample.blockchain;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import com.myexample.common.Property;
import com.myexample.common.Result;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SBChainTest {

    private static Path dataDir;
    private static Path chainfile;
    private static Path transactionsfile;

    private SBChain sbc;

    @BeforeClass
    public static void setUpClass() throws Exception {
        dataDir = Path.of(Property.getProperty("datadir"));
        chainfile = dataDir.resolve(Property.getProperty("chainfile"));
        transactionsfile = dataDir.resolve(Property.getProperty("transactionsfile"));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        Files.deleteIfExists(chainfile);
        Files.deleteIfExists(transactionsfile);
    }

    @Before
    public void setUp() throws Exception {
        Files.deleteIfExists(chainfile);
        Files.deleteIfExists(transactionsfile);
        sbc = new SBChain();
    }
    
    @Test
    public void addTransaction_2Args_TRANSACTION_SUCCESS() throws Exception {
        var expected = Result.TRANSACTION_SUCCESS;
        var actual = sbc.addTransaction("recipientAdr", new BigDecimal("1"));
        assertThat(actual, is(expected));
    }

    @Test
    public void addTransaction_2Args_AMOUNT_SCALE_OVERFLOW() throws Exception {
        var expected = Result.AMOUNT_SCALE_OVERFLOW;
        var actual = sbc.addTransaction(
            "recipientAdr", BigDecimal.ONE.scaleByPowerOfTen(-SBChain.TRANSACTION_AMOUNT_SCALE-1));
        assertThat(actual, is(expected));
    }

    @Test
    public void addTransaction_2Args_TOO_LARGE_AMOUNT() throws Exception {
        var expected = Result.TOO_LARGE_AMOUNT;
        var actual = sbc.addTransaction(
            "recipientAdr", SBChain.TRANSACTION_MAX_AMOUNT.add(BigDecimal.ONE.scaleByPowerOfTen(-SBChain.TRANSACTION_AMOUNT_SCALE)));
        assertThat(actual, is(expected));
    }

    @Test
    public void addTransaction_3Args_TRANSACTION_SUCCESS() throws Exception {
        var expected = Result.TRANSACTION_SUCCESS;
        var actual = sbc.addTransaction("recipientAdr", new BigDecimal("1"), "sign");
        assertThat(actual, is(expected));
    }

    @Test
    public void addTransaction_3Args_AMOUNT_SCALE_OVERFLOW() throws Exception {
        var expected = Result.AMOUNT_SCALE_OVERFLOW;
        var actual = sbc.addTransaction("recipientAdr", BigDecimal.ONE.scaleByPowerOfTen(-SBChain.TRANSACTION_AMOUNT_SCALE-1), "sign");
        assertThat(actual, is(expected));
    }

    @Test
    public void addTransaction_3Args_TOO_LARGE_AMOUNT() throws Exception {
        var expected = Result.TOO_LARGE_AMOUNT;
        var actual = sbc.addTransaction("recipientAdr", SBChain.TRANSACTION_MAX_AMOUNT.add(BigDecimal.ONE.scaleByPowerOfTen(-SBChain.TRANSACTION_AMOUNT_SCALE)), "sign");
        assertThat(actual, is(expected));
    }

    @Test
    public void addTransaction_3Args_SIGNATURE_ALREADY_CONSUMED() throws Exception {
        var expected = Result.SIGNATURE_ALREADY_CONSUMED;
        sbc.addTransaction("recipientAdr", new BigDecimal("1"), "sign");
        var actual = sbc.addTransaction("recipientAdr2", new BigDecimal("2"), "sign");
        assertThat(actual, is(expected));
    }

    @Test
    public void addTransaction_4Args_TRANSACTION_SUCCESS() throws Exception {
        var expected = Result.TRANSACTION_SUCCESS;
        sbc.addTransaction("adr", new BigDecimal("1"));
        var actual = sbc.addTransaction("adr", "recipientAdr", new BigDecimal("1"), "sign");
        assertThat(actual, is(expected));
    }

    @Test
    public void addTransaction_4Args_AMOUNT_SCALE_OVERFLOW() throws Exception {
        var expected = Result.AMOUNT_SCALE_OVERFLOW;
        sbc.addTransaction("adr", new BigDecimal("1"));
        var actual = sbc.addTransaction("adr", "recipientAdr", BigDecimal.ONE.scaleByPowerOfTen(-SBChain.TRANSACTION_AMOUNT_SCALE-1), "sign");
        assertThat(actual, is(expected));
    }

    @Test
    public void addTransaction_4Args_TOO_LARGE_AMOUNT() throws Exception {
        var expected = Result.TOO_LARGE_AMOUNT;
        sbc.addTransaction("adr", new BigDecimal("2"));
        var actual = sbc.addTransaction("recipientAdr", SBChain.TRANSACTION_MAX_AMOUNT.add(BigDecimal.ONE.scaleByPowerOfTen(-SBChain.TRANSACTION_AMOUNT_SCALE)), "sign");
        assertThat(actual, is(expected));
    }

    @Test
    public void addTransaction_4Args_NOT_ENOUGH_BALANCE() throws Exception {
        var expected = Result.NOT_ENOUGH_BALANCE;
        sbc.addTransaction("adr", new BigDecimal("1"));
        var actual = sbc.addTransaction("adr", "recipientAdr", new BigDecimal("1.1"), "sign");
        assertThat(actual, is(expected));
    }

    @Test
    public void addTransaction_4Args_SIGNATURE_ALREADY_CONSUMED() throws Exception {
        var expected = Result.SIGNATURE_ALREADY_CONSUMED;
        sbc.addTransaction("adr", new BigDecimal("2"), "sign");
        var actual = sbc.addTransaction("adr", "recipientAdr", new BigDecimal("1"), "sign");
        assertThat(actual, is(expected));
    }

    @Test
    public void mining_Done() throws Exception {
        sbc.addTransaction("recipientAdr", new BigDecimal("1"));
        sbc.mining();
        var expectedChainSize = 2;
        var actualChainSize = sbc.getChainSize();
        var expectedTransactionPoolSize = 0;
        var actualTransactionPoolSize = sbc.getTransactionPoolSize();
        assertThat(actualChainSize, is(expectedChainSize));
        assertThat(actualTransactionPoolSize, is(expectedTransactionPoolSize));
    }

    @Test
    public void mining_NotDone() throws Exception {
        sbc.mining();
        var expected = 1;
        var actual = sbc.getChainSize();
        assertThat(actual, is(expected));
    }

    @Test
    public void scheduleAutoMining_5SecondDelay() throws Exception {
        sbc.scheduleAutoMining(5, TimeUnit.SECONDS);
        Thread.sleep(2000);
        sbc.addTransaction("adr", new BigDecimal("1"));
        Thread.sleep(5000);
        sbc.waitUntilChainUnlocked();
        var expectedChainSize = 2;
        var actualChainSize = sbc.getChainSize();
        var expectedTransactionPoolSize = 0;
        var actualTransactionPoolSize = sbc.getTransactionPoolSize();
        assertThat(actualChainSize, is(expectedChainSize));
        assertThat(actualTransactionPoolSize, is(expectedTransactionPoolSize));
    }

    @Test
    public void calculateTotalAmount_Receive2_Send1() throws Exception {
        sbc.addTransaction("adr", new BigDecimal("2"));
        sbc.addTransaction("adr", "recipientAdr", new BigDecimal("1"), "sign");
        var expected = new BigDecimal("1").setScale(SBChain.TRANSACTION_AMOUNT_SCALE);
        var actual = sbc.calculateTotalAmount("adr");
        assertThat(actual, is(expected));
    }

    @Test
    public void isDuplicatedSignature_True() throws Exception {
        sbc.addTransaction("recipientAdr", new BigDecimal("1"), "sign");
        var actual = sbc.isDuplicatedSignature("sign");
        assertThat(actual, is(true));
    }

    @Test
    public void isDuplicatedSignature_False() throws Exception {
        sbc.addTransaction("recipientAdr", new BigDecimal("1"), "sign");
        var actual = sbc.isDuplicatedSignature("sign2");
        assertThat(actual, is(false));
    }

    @Test
    public void getAllTransactions_2InChain_1InPool() throws Exception {
        sbc.addTransaction("adr", new BigDecimal("2")); // transaction added
        sbc.mining(); // transaction added
        sbc.addTransaction("adr", "recipientAdr", new BigDecimal("1"), "sign"); // transaction added
        var expected = 3;
        var actual = sbc.getAllTransactions().size();
        assertThat(actual, is(expected));
    }
}
