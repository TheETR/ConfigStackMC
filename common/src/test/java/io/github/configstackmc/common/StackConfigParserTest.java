package io.github.configstackmc.common;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class StackConfigParserTest {
    @Test void parsesBareNamespacedAndQuotedIds() {
        StackConfig cfg = StackConfigParser.parse("""
max-allowed-stack-size: 99
items:
  dried_ghast: 64
  minecraft:totem_of_undying: 1
  "minecraft:ender_pearl": 16
""", ConfigLogger.noop());
        assertEquals(64, cfg.rule("minecraft:dried_ghast").orElseThrow().maxStackSize());
        assertEquals(1, cfg.rule("minecraft:totem_of_undying").orElseThrow().maxStackSize());
        assertEquals(16, cfg.rule("minecraft:ender_pearl").orElseThrow().maxStackSize());
    }
    @Test void rejectsInvalidIdAndSizes() {
        StackConfig cfg = StackConfigParser.parse("""
items:
  Bad Id: 64
  egg: 0
  snowball: -1
  ender_pearl: 100
""", ConfigLogger.noop());
        assertTrue(cfg.rules().isEmpty()); assertEquals(4, cfg.invalidRuleCount());
    }
    @Test void ignoresCommentsAndBlankLines() {
        StackConfig cfg = StackConfigParser.parse("""
# top
items:

  bell: 64 # comment
""", ConfigLogger.noop());
        assertEquals(1, cfg.rules().size());
    }
    @Test void unconfiguredItemsRemainAbsent() {
        StackConfig cfg = StackConfigParser.parse("items:\n  bell: 64\n", ConfigLogger.noop());
        assertTrue(cfg.rule("minecraft:oak_planks").isEmpty());
    }
}
