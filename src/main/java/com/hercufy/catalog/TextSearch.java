package com.hercufy.catalog;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Normalizacion y tokenizado de texto para la busqueda del catalogo. Es el mismo
 * criterio que usa el buscador del frontend (labels.ts: normalize/tokens), para que
 * el comportamiento no cambie cuando el frontend deje de usar su catalogo simulado
 * y empiece a llamar a esta API.
 */
public final class TextSearch {

    private static final Set<String> STOP_WORDS = Set.of("de", "con", "en", "el", "la", "los", "las", "a", "y", "un", "una");
    private static final Pattern MARKS = Pattern.compile("\\p{M}");
    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9\\s]");
    private static final Pattern SPACES = Pattern.compile("\\s+");

    private TextSearch() {
    }

    public static String normalize(String s) {
        if (s == null) {
            return "";
        }
        String n = Normalizer.normalize(s.toLowerCase(java.util.Locale.ROOT), Normalizer.Form.NFD);
        n = MARKS.matcher(n).replaceAll("");
        n = NON_ALNUM.matcher(n).replaceAll(" ");
        n = SPACES.matcher(n).replaceAll(" ").trim();
        return n;
    }

    public static List<String> tokens(String query) {
        return Arrays.stream(normalize(query).split(" "))
                .filter(t -> !t.isBlank() && !STOP_WORDS.contains(t))
                .map(t -> t.length() > 4 ? t.replaceAll("s$", "") : t)
                .toList();
    }

    public static boolean matchesAllTokens(String haystackNormalized, List<String> tokens) {
        for (String t : tokens) {
            if (!haystackNormalized.contains(t)) {
                return false;
            }
        }
        return true;
    }
}
