package com.github.watermoonlx.simpleRule.core;

import java.util.concurrent.CompletableFuture;

/**
 * 各子规则并行执行的RuleSet
 */
public class ParallelRuleSet<T> extends RuleSet<T> {

    public ParallelRuleSet(Operator operator,Rule<T>... rules) {
        super(operator, rules);
    }

    public ParallelRuleSet(Rule<T>... rules) {
        super(rules);
    }

    @Override
    public CompletableFuture<RuleCheckResult> checkAsync(T target, RuleEngine engine) {
        return engine.runAsync(this, target);
    }
}
