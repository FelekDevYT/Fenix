package me.felek.fenix;

import me.felek.fenix.error.FenixErrorListener;
import me.felek.fenix.preprocessor.Preprocessor;
import me.felek.fenix.struct.Modifier;
import me.felek.fenix.utils.TypeUtils;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String sourceCode = """
                println(1_000_178);
                """;
        String preprocessed = Preprocessor.preprocess(sourceCode);

        FenixVisitorImpl interpreter = new FenixVisitorImpl();
        FenixLexer lexer = new FenixLexer(CharStreams.fromString(preprocessed));
        FenixParser parser = new FenixParser(new CommonTokenStream(lexer));

//        FenixErrorListener errorListener = new FenixErrorListener(preprocessed);
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
