package me.felek.fenix;

import me.felek.fenix.error.FenixErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String sourceCode = """
                var a: Int = 4;
                if (a == 1) {
                    println("yes");
                } elif (a == 0) {
                    println("yep");
                } elif (a == 2) {
                    println("yea");
                } else {
                    println("no");
                }
                """;

        FenixVisitorImpl interpreter = new FenixVisitorImpl();
        FenixLexer lexer = new FenixLexer(CharStreams.fromString(sourceCode));
        FenixParser parser = new FenixParser(new CommonTokenStream(lexer));

//        FenixErrorListener errorListener = new FenixErrorListener(sourceCode);
//        lexer.removeErrorListeners();
//        lexer.addErrorListener(errorListener);
//        parser.removeErrorListeners();
//        parser.addErrorListener(errorListener);
//
//        ParseTree tree = parser.program();
//        if (errorListener.hasErrors()) {
//            errorListener.getErrors().forEach(System.out::println);
//            return;
//        }

        Object result = interpreter.visit(parser.program());
    }
}
