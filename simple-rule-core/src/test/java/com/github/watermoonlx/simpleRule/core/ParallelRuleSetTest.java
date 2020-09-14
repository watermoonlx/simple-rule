package com.github.watermoonlx.simpleRule.core;

import com.github.watermoonlx.simpleRule.utils.AssertUtils;
import lombok.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

public class ParallelRuleSetTest {
    public static class MockRule extends Rule<String> {

        private String passStr;

        public MockRule(String passStr) {
            this.passStr = passStr;
        }

        @Override
        public RuleCheckResult check(@NonNull String target) {
            if (target.equalsIgnoreCase(this.passStr)) {
                return this.pass("通过");
            } else {
                return this.error("不通过");
            }
        }
    }

    @Test
    public void testAndPass() {
        MockRule rule1 = new MockRule("A");
        MockRule rule2 = new MockRule("A");
        MockRule rule3 = new MockRule("A");
        MockRule rule4 = new MockRule("A");
        ParallelRuleSet<String> ruleSet = new ParallelRuleSet(rule1, rule2, rule3, rule4);

        RuleCheckResult result = ruleSet.check("A");

        RuleCheckResult expected = new RuleCheckResult();
        RuleCheckResultDetail detail1 = new RuleCheckResultDetail();
        detail1.setRuleName(SerialRuleSetTest.MockRule.class.getSimpleName());
        detail1.setMessage("通过");
        expected.addPassed(detail1);
        RuleCheckResultDetail detail2 = new RuleCheckResultDetail();
        detail2.setRuleName(SerialRuleSetTest.MockRule.class.getSimpleName());
        detail2.setMessage("通过");
        expected.addPassed(detail2);
        RuleCheckResultDetail detail3 = new RuleCheckResultDetail();
        detail3.setRuleName(SerialRuleSetTest.MockRule.class.getSimpleName());
        detail3.setMessage("通过");
        expected.addPassed(detail3);
        RuleCheckResultDetail detail4 = new RuleCheckResultDetail();
        detail4.setRuleName(SerialRuleSetTest.MockRule.class.getSimpleName());
        detail4.setMessage("通过");
        expected.addPassed(detail4);

        AssertUtils.assertResultEquals(result, expected);
    }

    @Test
    public void testAndError() {
        MockRule rule1 = new MockRule("A");
        MockRule rule2 = new MockRule("A");
        MockRule rule3 = new MockRule("A");
        MockRule rule4 = new MockRule("B");
        ParallelRuleSet<String> ruleSet = new ParallelRuleSet(rule1, rule2, rule3, rule4);

        RuleCheckResult result = ruleSet.check("A");

        RuleCheckResult expected = new RuleCheckResult();
        RuleCheckResultDetail detail1 = new RuleCheckResultDetail();
        detail1.setRuleName(SerialRuleSetTest.MockRule.class.getSimpleName());
        detail1.setMessage("通过");
        expected.addPassed(detail1);
        RuleCheckResultDetail detail2 = new RuleCheckResultDetail();
        detail2.setRuleName(SerialRuleSetTest.MockRule.class.getSimpleName());
        detail2.setMessage("通过");
        expected.addPassed(detail2);
        RuleCheckResultDetail detail3 = new RuleCheckResultDetail();
        detail3.setRuleName(SerialRuleSetTest.MockRule.class.getSimpleName());
        detail3.setMessage("通过");
        expected.addPassed(detail3);
        RuleCheckResultDetail detail4 = new RuleCheckResultDetail();
        detail4.setRuleName(SerialRuleSetTest.MockRule.class.getSimpleName());
        detail4.setMessage("不通过");
        expected.addError(detail4);

        AssertUtils.assertResultEquals(result, expected);
    }

    @Test
    public void testOrPass() {
        MockRule rule1 = new MockRule("A");
        MockRule rule2 = new MockRule("B");
        MockRule rule3 = new MockRule("A");
        ParallelRuleSet<String> ruleSet = new ParallelRuleSet(RuleSet.Operator.OR,rule1, rule2, rule3);

        RuleCheckResult result = ruleSet.check("B");

        RuleCheckResult expected = new RuleCheckResult();
        RuleCheckResultDetail detail1 = new RuleCheckResultDetail();
        detail1.setRuleName(SerialRuleSetTest.MockRule.class.getSimpleName());
        detail1.setMessage("通过");
        expected.addPassed(detail1);

        AssertUtils.assertResultEquals(result, expected);
    }

    @Test
    public void testOrError() {
        MockRule rule1 = new MockRule("A");
        MockRule rule2 = new MockRule("A");
        MockRule rule3 = new MockRule("A");
        ParallelRuleSet<String> ruleSet = new ParallelRuleSet(RuleSet.Operator.OR,rule1, rule2, rule3);

        RuleCheckResult result = ruleSet.check("B");

        RuleCheckResult expected = new RuleCheckResult();
        RuleCheckResultDetail detail1 = new RuleCheckResultDetail();
        detail1.setRuleName(SerialRuleSetTest.MockRule.class.getSimpleName());
        detail1.setMessage("不通过");
        expected.addError(detail1);
        RuleCheckResultDetail detail2 = new RuleCheckResultDetail();
        detail2.setRuleName(SerialRuleSetTest.MockRule.class.getSimpleName());
        detail2.setMessage("不通过");
        expected.addError(detail2);
        RuleCheckResultDetail detail3 = new RuleCheckResultDetail();
        detail3.setRuleName(SerialRuleSetTest.MockRule.class.getSimpleName());
        detail3.setMessage("不通过");
        expected.addError(detail3);

        AssertUtils.assertResultEquals(result, expected);
    }

    @Test
    public void testParallel() {
        class DelayRule extends Rule<String> {
            @Override
            public RuleCheckResult check(@NonNull String target) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return this.pass();
            }
        }

        ParallelRuleSet<String> ruleSet = new ParallelRuleSet<>(
                new DelayRule(),
                new DelayRule(),
                new DelayRule(),
                new DelayRule(),
                new DelayRule(),
                new DelayRule(),
                new DelayRule(),
                new DelayRule(),
                new DelayRule(),
                new DelayRule()
        );
        LocalDateTime start = LocalDateTime.now();
        RuleCheckResult result = ruleSet.checkAsync("Any").join();
        LocalDateTime end = LocalDateTime.now();
        long elapsed = Duration.between(start, end).toMillis();
        Assertions.assertTrue(elapsed < 3000);
    }
}
