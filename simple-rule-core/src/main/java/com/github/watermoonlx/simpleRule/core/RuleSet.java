package com.github.watermoonlx.simpleRule.core;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

/**
 * 规则集基类。
 * 规则集是Rule类的集合，它负责依次执行各个Rule的验证，并收集错误信息。一个规则集中的规则要么都执行，要么都不执行。
 * 其本身也是一个Rule类。这采用了“组合模式”。请参考:https://www.cnblogs.com/chenssy/p/3299719.html.
 * 采用“组合模式”的目的，是为了能对业务规则进行更为灵活的组合。比如可以将一个RuleSet整体加入另一个RuleSet。
 * 可以直接new一个RuleSet，然后调用register方法添加任意数量的Rule实例。然后调用Check方法进行验证。
 * 但是，推荐根据具体的业务场景，创建一个独立的RuleSet类，封装该业务场景所需的所有Rule。
 */
@NoArgsConstructor
abstract class RuleSet<T> extends Rule<T> {
    @Getter
    private final ArrayList<Rule<T>> subRules = new ArrayList<>();
    @Getter
    private Operator operator = Operator.AND;

    public RuleSet(Operator operator, Rule<T>... rules) {
        this(rules);
        this.operator = operator;
    }

    public RuleSet(Rule<T>... rules) {
        for (Rule<T> rule : rules) {
            this.register(rule);
        }
    }

    public void register(Rule<T> rule) {
        this.subRules.add(rule);
    }

    @Override
    public RuleCheckResult check(T target) {
        return this.checkAsync(target).join();
    }

    public static enum Operator {
        AND,
        OR
    }
}
