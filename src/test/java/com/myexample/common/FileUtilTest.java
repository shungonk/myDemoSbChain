package com.myexample.common;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Files;

import com.myexample.util.FileUtil;

import org.junit.Test;

public class FileUtilTest {

    @Test
    public void serialize_And_Deserialize() throws Exception {
        var expected = "test_FileUtil";
        var temp = Files.createTempFile(null, null);
        FileUtil.serializeObject(temp, expected);
        var actual = (String) FileUtil.deserializeObject(temp);
        assertThat(actual, is(expected));
    }
    
}
