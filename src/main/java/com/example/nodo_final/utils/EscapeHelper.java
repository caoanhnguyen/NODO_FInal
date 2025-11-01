package com.example.nodo_final.utils;

public class EscapeHelper {
    public static String escapeLike(String param) {
        if (param == null) {
            return null;
        }
        return param.replace("\\", "\\\\")
                    .replace("_", "\\_")
                    .replace("%", "\\%");
    }
}
