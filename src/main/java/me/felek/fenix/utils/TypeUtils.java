package me.felek.fenix.utils;

import me.felek.fenix.exceptions.FenixUnknownModifierException;
import me.felek.fenix.struct.Modifier;
import me.felek.fenix.type.Value;
import me.felek.fenix.type.ValueType;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TypeUtils {
    public static ValueType getTypeFromString(String type) {
        switch (type.toLowerCase()) {
            case "int" -> {
                return ValueType.INT;
            }
            case "string" -> {
                return ValueType.STRING;
            }
            case "bool" -> {
                return ValueType.BOOL;
            }
            case "float" -> {
                return ValueType.FLOAT;
            }
            case "obj" -> {
                return ValueType.OBJECT;
            }
            default -> {
                return ValueType.NULL;
            }
        }
    }

    public static Modifier getModifier(String mod) {
        switch (mod.toLowerCase()) {
            case "pub" -> {
                return Modifier.PUB;
            }
            case "static" -> {
                return Modifier.STATIC;
            }
            case "loc" -> {
                return Modifier.LOC;
            }
            default -> {
                throw new FenixUnknownModifierException(mod);
            }
        }
    }

    public static List<Modifier> parseModifiers(List<TerminalNode> tns) {
        List<String> strs =new ArrayList<>();
        for (var tn : tns) {
            strs.add(tn.getText());
        }

        List<Modifier> modifiers = new ArrayList<>();
        for (String s : strs) {
            modifiers.add(getModifier(s));
        }

        return modifiers;
    }
}
