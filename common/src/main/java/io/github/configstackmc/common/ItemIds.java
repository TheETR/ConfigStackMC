package io.github.configstackmc.common;

import java.util.Locale;
import java.util.regex.Pattern;

public final class ItemIds {
    private static final Pattern ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");
    private ItemIds() {}
    public static String normalize(String raw) {
        if (raw == null) throw new IllegalArgumentException("Item id is blank");
        String id = unquote(raw.trim()).toLowerCase(Locale.ROOT);
        if (id.isEmpty()) throw new IllegalArgumentException("Item id is blank");
        if (!id.contains(":")) id = "minecraft:" + id;
        if (!ID.matcher(id).matches()) throw new IllegalArgumentException("Invalid item id: " + raw);
        return id;
    }
    public static String path(String normalized) { return normalized.substring(normalized.indexOf(':') + 1); }
    private static String unquote(String value) {
        if (value.length() >= 2 && ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")))) return value.substring(1, value.length() - 1);
        return value;
    }
}
