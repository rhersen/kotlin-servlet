package main;

import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class Tag {

    static Tag t(String tagName, String content, List<String> attr) {
        return new Tag(tagName, singletonList(content), attr);
    }

    static Tag t(String tagName, Collection contents, List<String> attr) {
        return new Tag(tagName, contents, attr);
    }

    static Tag t(String tagName, String content) {
        return new Tag(tagName, singletonList(content), emptyList());
    }

    static Tag t(String tagName, Tag content) {
        return new Tag(tagName, singletonList(content), emptyList());
    }

    static Tag t(String tagName, Collection contents) {
        return new Tag(tagName, contents, emptyList());
    }

    static List<Tag> c(Tag... tags) {
        return asList(tags);
    }

    static List<String> a(String key, String value) {
        return asList(key, value);
    }

    private String name;
    private final Collection contents;
    private List<String> attr;

    private Tag(String name, Collection contents, List<String> attr) {
        this.name = name;
        this.contents = contents;
        this.attr = attr;
    }

    @SuppressWarnings("unchecked")
    public String toString() {
        StringBuilder b = new StringBuilder();

        b.append("<").append(name);

        if (!attr.isEmpty())
            b.append(" ").append(attr.get(0)).append("=").append(attr.get(1));

        b.append(">");

        contents.forEach(b::append);

        if (!contents.isEmpty())
            b.append("</").append(name).append(">");

        return b.toString();
    }
}
