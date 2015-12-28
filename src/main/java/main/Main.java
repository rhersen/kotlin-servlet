package main;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static java.util.Arrays.asList;
import static main.Tag.t;

public class Main extends javax.servlet.http.HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter w = resp.getWriter();
        Tag t = t("html",
                asList(t("head",
                        asList(t("meta", "", "charset", "utf-8"),
                                t("title", "blah"))),
                        t("body",
                                t("p", "I am the content"))));
        writeHtml(w, t);
    }

    private void writeHtml(PrintWriter w, Tag t) {
        w.write("<!doctype html>");
        w.write(t.toString());
    }
}

