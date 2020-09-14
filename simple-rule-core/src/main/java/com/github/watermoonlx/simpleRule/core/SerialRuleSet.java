package com.github.watermoonlx.simpleRule.core;

import java.util.concurrent.CompletableFuture;

/**
 * 各子规则串行执行的RuleSet
 */
public class SerialRuleSet<T> extends RuleSet<T> {

    public SerialRuleSet(Operator operator, Rule<T>... rules) {
        super(operator, rules);
    }

    public SerialRuleSet(Rule<T>... rules) {
        super(rules);
    }

    @Override
    public CompletableFuture<RuleCheckResult> checkAsync(T target, RuleEngine engine) {
        return engine.runAsync(this, target);
    }
}
