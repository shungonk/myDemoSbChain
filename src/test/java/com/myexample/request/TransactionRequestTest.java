package com.myexample.request;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.Security;

import com.myexample.util.SecurityUtil;
import com.myexample.util.StringUtil;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

public class TransactionRequestTest {

    private static KeyPair keyPair;
    private static String pvt;
    private static String pub;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        keyPair = SecurityUtil.generateKeyPair();
        pvt = SecurityUtil.encodeKeyToString(keyPair.getPrivate());
        pub = SecurityUtil.encodeKeyToString(keyPair.getPublic());
    }

    @Test
    public void getData() throws Exception {
        var input = new TransactionRequest("test senderAdr", "test recipientAdr", BigDecimal.ONE, 1614908880604L);
        var expected = ("test senderAdrtest recipientAdr11614908880604").getBytes();
        var actual = input.getData();
        assertThat(actual, is(expected));
    }

    @Test
    public void validateFields_True() throws Exception {
        var input = new TransactionRequest("test senderAdr", "test recipientAdr", BigDecimal.ONE, 1614908880604L);
        input.signate(pvt, pub);
        assertThat(input.validateFields(), is(true));
    }

    @Test
    public void validateFields_False_NotSignated() throws Exception {
        var input = new TransactionRequest("test senderAdr", "test recipientAdr", BigDecimal.ONE, 1614908880604L);
        assertThat(input.validateFields(), is(false));
    }

    @Test
    public void validateAmount_True() throws Exception {
        var input = new TransactionRequest("test senderAdr", "test recipientAdr", BigDecimal.ONE, 1614908880604L);
        assertThat(input.validateAmount(), is(true));
    }

    @Test
    public void validateAmount_False_Zero() throws Exception {
        var input = new TransactionRequest("test senderAdr", "test recipientAdr", BigDecimal.ZERO, 1614908880604L);
        assertThat(input.validateAmount(), is(false));
    }

    @Test
    public void validateAmount_False_Negative() throws Exception {
        var input = new TransactionRequest("test senderAdr", "test recipientAdr", BigDecimal.ONE.negate(), 1614908880604L);
        assertThat(input.validateAmount(), is(false));
    }

    @Test
    public void verifyAddress_True() throws Exception {
        var adr = SecurityUtil.getAddressFromPublicKey(keyPair.getPublic());
        var input = new TransactionRequest(adr, "test recipientAdr", BigDecimal.ONE, 1614908880604L);
        input.signate(pvt, pub);
        assertThat(input.verifyAddress(), is(true));
    }

    @Test
    public void verifyAddress_False_InvalidAddress() throws Exception {
        var input = new TransactionRequest("swindlerAdr", "test recipientAdr", BigDecimal.ONE, 1614908880604L);
        input.signate(pvt, pub);
        assertThat(input.verifyAddress(), is(false));
    }

    @Test
    public void verifySignature_True() throws Exception {
        var input = new TransactionRequest("test senderAdr", "test recipientAdr", BigDecimal.ONE, 1614908880604L);
        input.signate(pvt, pub);
        assertThat(input.verifySignature(), is(true));
    }

    @Test
    public void verifySignature_False_DataReplaced() throws Exception {
        var input = new TransactionRequest("test senderAdr", "test recipientAdr", BigDecimal.ONE, 1614908880604L);
        input.signate(pvt, pub);
        var json = input.toJson();
        var jsonReplaced = json.replace("test recipientAdr", "swindlerAdr");
        var inputReplaced = StringUtil.fromJson(jsonReplaced, TransactionRequest.class);
        assertThat(inputReplaced.verifySignature(), is(false));
    }
    
}
