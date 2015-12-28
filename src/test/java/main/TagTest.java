package main;

import org.junit.Test;

import static java.util.Arrays.asList;
import static main.Tag.t;
import static org.junit.Assert.*;

public class TagTest {

    @Test
    public void basic() throws Exception {
        Tag subject = t("name", "text");
        String result = "" + subject;
        assertEquals("<name>text</name>", result);
    }

    @Test
    public void noEndTagIfContentIsNull() throws Exception {
        Tag subject = t("name", null, "class", "sub");
        String result = "" + subject;
        assertEquals("<name class=sub></name>", result);
    }

    @Test
    public void nestOne() throws Exception {
        Tag subject = t("tr", t("td", "data"));
        String result = "" + subject;
        assertEquals("<tr><td>data</td></tr>", result);
    }

    @Test
    public void nestTwo() throws Exception {
        Tag subject = t("tr", asList(t("td", "hello"), t("td", "world")));
        String result = "" + subject;
        assertEquals("<tr><td>hello</td><td>world</td></tr>", result);
    }
}