package main;

import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class Tag {

    static Tag t(String tagName, String content, String attrName, String attrValue) {
        return new Tag(tagName, singletonList(content), attrName, attrValue);
    }

    static Tag t(String tagName, Collection contents, String attrName, String attrValue) {
        return new Tag(tagName, contents, attrName, attrValue);
    }

    static Tag t(String tagName, String content) {
        return new Tag(tagName, singletonList(content), null, null);
    }

    static Tag t(String tagName, Tag content) {
        return new Tag(tagName, singletonList(content), null, null);
    }

    static Tag t(String tagName, Collection contents) {
        return new Tag(tagName, contents, null, null);
    }

    static List<Tag> c(Tag... tags) {
        return asList(tags);
    }

    private String name;
    private final Collection contents;
    private String attrName;
    private final String attrValue;

    private Tag(String name, Collection contents, String attrName, String attrValue) {
        this.name = name;
        this.contents = contents;
        this.attrName = attrName;
        this.attrValue = attrValue;
    }

    @SuppressWarnings("unchecked")
    public String toString() {
        StringBuilder b = new StringBuilder();

        b.append("<").append(name);

        if (attrName != null)
            b.append(" ").append(attrName).append("=").append(attrValue);

        b.append(">");

        contents.forEach(b::append);

        if (!contents.isEmpty())
            b.append("</").append(name).append(">");

        return b.toString();
    }
}
