package com.myexample.common;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

import org.junit.Test;

public class StringUtilTest {

    @Test
    public void repeat_5Times() throws Exception {
        var expected = "ttttt";
        var actual = StringUtil.repeat("t", 5);
        assertThat(actual, is(expected));
    }

    @Test
    public void repeat_0Times() throws Exception {
        var expected = "";
        var actual = StringUtil.repeat("t", 0);
        assertThat(actual, is(expected));
    }

    @Test
    public void makeJson_1Entry() throws Exception {
        var expected = "{\"name\":\"Tom\"}" ;
        var actual = StringUtil.makeJson("name", "Tom");
        assertThat(actual, is(expected));
    }

    @Test
    public void makeJson_2Enties() throws Exception {
        var expected = "{\"name\":\"Tom\",\"age\":\"20\"}" ;
        var actual = StringUtil.makeJson("name", "Tom", "age", "20");
        assertThat(actual, is(expected));
    }

    @Test
    public void makeJson_MapEntrySet() throws Exception {
        var expected = "{\"name\":\"Tom\",\"age\":\"20\",\"country\":\"America\"}" ;
        var input = new LinkedHashMap<String, String>();
        input.put("name", "Tom");
        input.put("age", "20");
        input.put("country", "America");
        var actual = StringUtil.makeJson(input);
        assertThat(actual, is(expected));
    }

    @Test
    public void valueInJson() throws Exception {
        var expected = "20" ;
        var input = "{\"name\":\"Tom\",\"age\":\"20\"}" ;
        var actual = StringUtil.valueInJson(input, "age");
        assertThat(actual, is(expected));
    }

    @Test
    public void formatJson() throws Exception {
        var expected = "name:Tom\nage:20\ncountry:America";
        var input = "{\"name\":\"Tom\",\"age\":\"20\",\"country\":\"America\"}" ;
        var actual = StringUtil.formatJson(input);
        assertThat(actual, is(expected));
    }

    @Test
    public void splitQuery() throws Exception {
        var expected = new LinkedHashMap<String, String>();
        expected.put("name", "Tom");
        expected.put("age", "20");
        expected.put("country", "America");
        var input  = "name=Tom&age=20&country=America";
        var actual = StringUtil.splitQuery(input);
        assertThat(actual, is(expected));
    }

    @Test
    public void formatDecimal() throws Exception {
        var expected = "1,234.567890";
        var input = new BigDecimal("1234.56789");
        var actual = StringUtil.formatDecimal(input, 6);
        assertThat(actual, is(expected));
    }
}
