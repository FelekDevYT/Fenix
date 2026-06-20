package me.felek.fenix.preprocessor;

import me.felek.fenix.exceptions.pre.FenixInvalidPreprocessorArgumentCountError;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Preprocessor {
    public static String preprocess(String code) {
        Map<String, String> macros = new HashMap<>();

        String[] lines = code.split("\n");
        StringBuilder processedCode = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String ready = lines[i].trim();
            int first = ready.split(" ")[0].length();
            String[] parts = ready.substring(first).split(" ");
            parts = Arrays.copyOfRange(parts, 1, parts.length);
            if (ready.startsWith("#define")) {
                if (parts.length != 2) {
                    throw new FenixInvalidPreprocessorArgumentCountError("define");
                }

                macros.put(parts[0], parts[1]);
                continue;
            }
            String rl = ready;
            for (String key : macros.keySet()) {
                ready = ready.replaceAll(key, macros.get(key));
            }

            processedCode.append(ready + "\n");
        }

        return processedCode.toString();
    }
}
