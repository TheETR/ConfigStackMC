package io.github.configstackmc.common;

import java.util.*;

public final class StackConfigParser {
    public static final int GAME_MAX_STACK_SIZE = 99;
    public static final String DEFAULT_CONFIG = """
# ConfigStackMC default config. Values below are vanilla defaults for the Minecraft version this jar was built for.
# Editing a value changes only newly created or normalized stacks of that configured item.
max-allowed-stack-size: 99
warn-dangerous-items: true
items:
  dried_ghast: 64
  bell: 64
  ender_pearl: 16
  snowball: 16
  egg: 16
  totem_of_undying: 1
  saddle: 1
  minecart: 1
  oak_boat: 1
  spruce_boat: 1
  birch_boat: 1
  jungle_boat: 1
  acacia_boat: 1
  dark_oak_boat: 1
  mangrove_boat: 1
  cherry_boat: 1
  bamboo_raft: 1
  water_bucket: 1
  lava_bucket: 1
  milk_bucket: 1
  powder_snow_bucket: 1
  armor_stand: 16
  sign: 16
  hanging_sign: 16
""";
    private static final Set<String> DANGEROUS = Set.of("shulker_box", "bundle", "written_book", "filled_map");
    private StackConfigParser() {}
    public static StackConfig parse(String text, ConfigLogger logger) {
        int maxAllowed = GAME_MAX_STACK_SIZE, invalid = 0; boolean warnDangerous = true, inItems = false; Map<String, StackRule> rules = new LinkedHashMap<>();
        for (String rawLine : text.split("\\R")) {
            String line = stripComment(rawLine);
            if (line.isBlank()) continue;
            String trimmed = line.trim();
            if (trimmed.equals("items:")) { inItems = true; continue; }
            int sep = trimmed.lastIndexOf(':');
            if (sep < 0) { logger.warn("Ignoring malformed config line: " + rawLine.trim()); invalid++; continue; }
            String key = trimmed.substring(0, sep).trim(); String value = trimmed.substring(sep + 1).trim();
            if (!inItems) {
                if (key.equals("max-allowed-stack-size")) maxAllowed = parseTopInt(value, GAME_MAX_STACK_SIZE, logger);
                else if (key.equals("warn-dangerous-items")) warnDangerous = Boolean.parseBoolean(value);
                continue;
            }
            try {
                String id = ItemIds.normalize(key); int size = Integer.parseInt(value);
                int effectiveMax = Math.min(maxAllowed, GAME_MAX_STACK_SIZE);
                if (size < 1 || size > effectiveMax) { logger.warn("Rejecting " + id + ": stack size " + size + " is outside 1-" + effectiveMax); invalid++; continue; }
                if (warnDangerous && isDangerous(id)) logger.warn("Configured " + id + " may have gameplay consequences when stacked because it can store data or durability.");
                rules.put(id, new StackRule(id, size));
            } catch (Exception ex) { logger.warn("Rejecting item rule '" + trimmed + "': " + ex.getMessage()); invalid++; }
        }
        return new StackConfig(Math.min(maxAllowed, GAME_MAX_STACK_SIZE), warnDangerous, rules, invalid);
    }
    private static int parseTopInt(String value, int fallback, ConfigLogger logger) { try { int parsed = Integer.parseInt(value); return parsed < 1 ? fallback : Math.min(parsed, GAME_MAX_STACK_SIZE); } catch (NumberFormatException ex) { logger.warn("Invalid max-allowed-stack-size, using " + fallback); return fallback; } }
    private static boolean isDangerous(String id) { String path = ItemIds.path(id); return DANGEROUS.stream().anyMatch(path::contains) || path.endsWith("_sword") || path.endsWith("_pickaxe") || path.endsWith("_axe") || path.endsWith("_shovel") || path.endsWith("_hoe") || path.endsWith("_helmet") || path.endsWith("_chestplate") || path.endsWith("_leggings") || path.endsWith("_boots"); }
    private static String stripComment(String line) {
        boolean quote = false; char qc = 0; StringBuilder out = new StringBuilder();
        for (int i=0;i<line.length();i++){ char c=line.charAt(i); if ((c=='\"'||c=='\'') && (i==0 || line.charAt(i-1)!='\\')) { if(!quote){quote=true;qc=c;} else if(qc==c) quote=false; } if(c=='#'&&!quote) break; out.append(c); }
        return out.toString();
    }
}
