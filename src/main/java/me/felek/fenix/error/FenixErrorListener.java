package me.felek.fenix.error;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.List;

public class FenixErrorListener extends BaseErrorListener {
    private List<String> errors = new ArrayList<>();
    private String source;

    public FenixErrorListener(String source) {
        this.source = source;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        //Error in [line:cpil] -> msg
        // println(a)
        //      ----^
        errors.add(String.format("""
                Error in [%d:%d] -> %s
                 %s
                """, line - 1, charPositionInLine, msg, source.split("\n")[line - 2]));
    }

    public boolean hasErrors() {return !errors.isEmpty();}
    public List<String> getErrors() {return errors;}
}
