package io.github.configstackmc.common;

import java.util.*;

public final class StackConfig {
    private final int maxAllowedStackSize;
    private final boolean warnDangerousItems;
    private final Map<String, StackRule> rules;
    private final int invalidRuleCount;
    public StackConfig(int maxAllowedStackSize, boolean warnDangerousItems, Map<String, StackRule> rules, int invalidRuleCount) {
        this.maxAllowedStackSize = maxAllowedStackSize; this.warnDangerousItems = warnDangerousItems;
        this.rules = Collections.unmodifiableMap(new LinkedHashMap<>(rules)); this.invalidRuleCount = invalidRuleCount;
    }
    public int maxAllowedStackSize() { return maxAllowedStackSize; }
    public boolean warnDangerousItems() { return warnDangerousItems; }
    public Map<String, StackRule> rules() { return rules; }
    public Optional<StackRule> rule(String normalizedItemId) { return Optional.ofNullable(rules.get(normalizedItemId)); }
    public int invalidRuleCount() { return invalidRuleCount; }
}
