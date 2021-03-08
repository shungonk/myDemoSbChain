package com.myexample.common;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.security.KeyPair;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

public class SecutiryUtilTest {

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
    public void sha256_Is64Hexadecimal() throws Exception {
        var pattern = "[0-9a-f]{64}";
        var actual = SecurityUtil.sha256("test");
        assertThat(actual.matches(pattern), is(true));
    }

    @Test
    public void encodeKeyToString_IsBase64() throws Exception {
        var pattern = "[0-9a-zA-Z+/=]*";
        var actual = SecurityUtil.encodeKeyToString(keyPair.getPrivate());
        assertThat(actual.matches(pattern), is(true));
    }

    @Test
    public void verifyEcdsaSign_True() throws Exception {
        var data = "test".getBytes();
        var sign = SecurityUtil.createEcdsaSign(pvt, data);
        var actual = SecurityUtil.verifyEcdsaSign(pub, data, sign);
        assertThat(actual, is(true));
    }

    @Test
    public void verifyEcdsaSign_False_NotSameData() throws Exception {
        var data = "test".getBytes();
        var sign = SecurityUtil.createEcdsaSign(pvt, data);
        var actual = SecurityUtil.verifyEcdsaSign(pub, "test2".getBytes(), sign);
        assertThat(actual, is(false));
    }

    @Test
    public void verifyEcdsaSign_False_InvalidKeyPair() throws Exception {
        var data = "test".getBytes();
        var sign = SecurityUtil.createEcdsaSign(pvt, data);
        var pub2 = SecurityUtil.encodeKeyToString(SecurityUtil.generateKeyPair().getPublic());
        var actual = SecurityUtil.verifyEcdsaSign(pub2, data, sign);
        assertThat(actual, is(false));
    }

    @Test
    public void getAddressFromPublicKey_IsBase58() throws Exception {
        var actual = SecurityUtil.getAddressFromPublicKey(keyPair.getPublic());
        var pattern = "[123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz]*";
        assertThat(actual.matches(pattern), is(true));
    }
}
