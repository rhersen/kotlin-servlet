package main;

import org.junit.Test;

import java.util.Collection;

import static java.util.Arrays.asList;
import static main.Tag.c;
import static main.Tag.t;
import static org.junit.Assert.*;

public class TagTest {

    @Test
    public void tagWithText() throws Exception {
        Tag subject = t("name", "text");
        String result = "" + subject;
        assertEquals("<name>text</name>", result);
    }

    @Test
    public void attribute() throws Exception {
        Tag subject = t("name", "text", "class", "sub");
        String result = "" + subject;
        assertEquals("<name class=sub>text</name>", result);
    }

    @Test
    public void noEndTagIfNoContent() throws Exception {
        Tag subject = t("name", c());
        String result = "" + subject;
        assertEquals("<name>", result);
    }

    @Test
    public void tagAsContent() throws Exception {
        Tag subject = t("tr", t("td", "data"));
        String result = "" + subject;
        assertEquals("<tr><td>data</td></tr>", result);
    }

    @Test
    public void twoTagsAsContent() throws Exception {
        Tag subject = t("tr", c(t("td", "hello"), t("td", "world")));
        String result = "" + subject;
        assertEquals("<tr><td>hello</td><td>world</td></tr>", result);
    }
}