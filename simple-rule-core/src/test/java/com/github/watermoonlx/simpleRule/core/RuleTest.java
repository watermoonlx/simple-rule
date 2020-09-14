package com.github.watermoonlx.simpleRule.core;

import com.github.watermoonlx.simpleRule.utils.AssertUtils;
import lombok.NonNull;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class RuleTest {
    public static class MockRule extends Rule<String> {

        @Setter
        private int delay = 0;

        public MockRule() {
        }

        public MockRule(@NonNull String name) {
            this(name, null);
        }

        public MockRule(@NonNull String name, String desc) {
            this.setName(name);
            if (desc != null) {
                this.setDescription(desc);
            }
        }

        @Override
        public RuleCheckResult check(@NonNull String target) {
            if (this.delay > 0) {
                try {
                    Thread.sleep(this.delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (target.equalsIgnoreCase("pass")) {
                return this.pass("验证通过");
            } else if (target.equalsIgnoreCase("warning")) {
                return this.warning("警告");
            } else if (target.equalsIgnoreCase("error")) {
                return this.error("错误");
            } else {
                throw new RuntimeException("非法字符");
            }
        }
    }

    private static RuleCheckResult passResult;
    private static RuleCheckResult warningResult;
    private static RuleCheckResult errorResult;

    @BeforeAll
    public static void setUp() {
        passResult = new RuleCheckResult();
        RuleCheckResultDetail passDetail = new RuleCheckResultDetail();
        passDetail.setRuleName(MockRule.class.getSimpleName());
        passDetail.setMessage("验证通过");
        passResult.addPassed(passDetail);

        warningResult = new RuleCheckResult();
        RuleCheckResultDetail warningDetail = new RuleCheckResultDetail();
        warningDetail.setRuleName(MockRule.class.getSimpleName());
        warningDetail.setMessage("警告");
        warningResult.addWarning(warningDetail);

        errorResult = new RuleCheckResult();
        RuleCheckResultDetail errorDetail = new RuleCheckResultDetail();
        errorDetail.setRuleName(MockRule.class.getSimpleName());
        errorDetail.setMessage("错误");
        errorResult.addError(errorDetail);
    }

    @Test
    public void testCheck() {
        MockRule rule = new MockRule();
        RuleCheckResult result = null;

        result = rule.check("pass");
        AssertUtils.assertResultEquals(result, passResult);

        result = rule.check("warning");
        AssertUtils.assertResultEquals(result, warningResult);

        result = rule.check("error");
        AssertUtils.assertResultEquals(result, errorResult);
    }

    @Test
    public void testCheckAsync() {
        MockRule rule = new MockRule();
        rule.setDelay(3000);

        CompletableFuture<RuleCheckResult> future1 = rule.checkAsync("pass");
        CompletableFuture<RuleCheckResult> future2 = rule.checkAsync("warning");
        CompletableFuture<RuleCheckResult> future3 = rule.checkAsync("error");

        AssertUtils.assertResultEquals(future1.join(), passResult);
        AssertUtils.assertResultEquals(future2.join(), warningResult);
        AssertUtils.assertResultEquals(future3.join(), errorResult);
    }

    @Test
    public void testAnd() {
        MockRule ruleA = new MockRule();
        MockRule ruleB = new MockRule();
        SerialRuleSet<String> ruleC = ruleA.and(ruleB);

        Assertions.assertEquals(ruleC.getSubRules().size(), 2);
        Assertions.assertEquals(ruleC.getSubRules().get(0), ruleA);
        Assertions.assertEquals(ruleC.getSubRules().get(1), ruleB);
    }

    @Test
    public void testOr() {
        MockRule ruleA = new MockRule();
        MockRule ruleB = new MockRule();
        SerialRuleSet<String> ruleC = ruleA.or(ruleB);

        Assertions.assertEquals(ruleC.getOperator(), RuleSet.Operator.OR);
        Assertions.assertEquals(ruleC.getSubRules().size(), 2);
        Assertions.assertEquals(ruleC.getSubRules().get(0), ruleA);
        Assertions.assertEquals(ruleC.getSubRules().get(1), ruleB);
    }

    @Test
    public void testSerial() {
        MockRule ruleA = new MockRule();
        MockRule ruleB = new MockRule();
        MockRule ruleC = new MockRule();
        MockRule ruleD = new MockRule();
        SerialRuleSet<String> ruleSet = Rule.serial(ruleA, ruleB, ruleC, ruleD);

        Assertions.assertEquals(ruleSet.getOperator(), RuleSet.Operator.AND);
        Assertions.assertEquals(ruleSet.getSubRules().size(), 4);
        Assertions.assertEquals(ruleSet.getSubRules().get(0), ruleA);
        Assertions.assertEquals(ruleSet.getSubRules().get(1), ruleB);
        Assertions.assertEquals(ruleSet.getSubRules().get(2), ruleC);
        Assertions.assertEquals(ruleSet.getSubRules().get(3), ruleD);
    }

    @Test
    public void testParallel() {
        MockRule ruleA = new MockRule();
        MockRule ruleB = new MockRule();
        MockRule ruleC = new MockRule();
        MockRule ruleD = new MockRule();
        ParallelRuleSet<String> ruleSet = Rule.parallel(ruleA, ruleB, ruleC, ruleD);

        Assertions.assertEquals(ruleSet.getOperator(), RuleSet.Operator.AND);
        Assertions.assertEquals(ruleSet.getSubRules().size(), 4);
        Assertions.assertEquals(ruleSet.getSubRules().get(0), ruleA);
        Assertions.assertEquals(ruleSet.getSubRules().get(1), ruleB);
        Assertions.assertEquals(ruleSet.getSubRules().get(2), ruleC);
        Assertions.assertEquals(ruleSet.getSubRules().get(3), ruleD);
    }

    @Test
    public void testDescriptor() {
        @Rule.Descriptor("这是一个测试类")
        class DescRule extends Rule<String> {
            @Override
            public RuleCheckResult check(@NonNull String target) {
                return null;
            }
        }

        String description = new DescRule().getDescription();
        Assertions.assertEquals("这是一个测试类", description);

        @Rule.Descriptor(name = "测试类", value = "这是一个测试类")
        class NameAndDescRule extends Rule<String> {
            @Override
            public RuleCheckResult check(@NonNull String target) {
                return null;
            }
        }

        NameAndDescRule nameAndDescRule = new NameAndDescRule();
        Assertions.assertEquals("测试类", nameAndDescRule.getName());
        Assertions.assertEquals("这是一个测试类", nameAndDescRule.getDescription());
    }

    @Test
//    @Disabled("for demonstration purposes")
    public void testDrawImage() throws IOException {
        MockRule rule1 = new MockRule("Rule 1", "测试规则1");
        MockRule rule2 = new MockRule("Rule 2", "测试规则2");
        MockRule rule3 = new MockRule("Rule 3", "测试规则3");
        MockRule rule4 = new MockRule("Rule 4", "测试规则4");
        MockRule rule5 = new MockRule("Rule 5", "测试规则5");
        MockRule rule6 = new MockRule("Rule 6", "测试规则6");
        MockRule rule7 = new MockRule("Rule 7", "测试规则7");

        rule1.drawImage(this.getRandomPath());
        rule1.and(rule2).and(rule3).drawImage(this.getRandomPath());
        rule1.or(rule2).or(rule3).drawImage(this.getRandomPath());
        Rule.parallel(rule1, rule2, rule2).drawImage(this.getRandomPath());

        Rule.serial(
                rule4,
                Rule.parallel(rule1, rule2, rule2)
        ).drawImage(this.getRandomPath());
        Rule.serial(
                rule4,
                Rule.parallel(
                        Rule.parallel(rule1, rule2, rule2),
                        rule5
                )
        ).drawImage(this.getRandomPath());
        Rule.serial(
                rule1,
                Rule.parallel(
                        Rule.parallel(rule2,
                                Rule.serial(rule3, rule4),
                                Rule.parallel(rule5, rule6)),
                        rule7
                )
        ).drawImage(this.getRandomPath());
        Rule.serial(
                rule1,
                Rule.parallel(
                        Rule.parallel(
                                rule2,
                                Rule.serial(RuleSet.Operator.OR, rule3, rule4),
                                Rule.parallel(RuleSet.Operator.OR, rule5, rule6)),
                        rule7
                )
        ).drawImage(this.getRandomPath());
    }

    @Test
//    @Disabled("for demonstration purposes")
    public void testDrawImageWithResult() throws IOException {
        RuleEngine engine = new RuleEngine();
        MockRule rule1 = new MockRule("Rule 1", "测试规则1");
        MockRule rule2 = new MockRule("Rule 2", "测试规则2");
        MockRule rule3 = new MockRule("Rule 3", "测试规则3");
        MockRule rule4 = new MockRule("Rule 4", "测试规则4");
        MockRule rule5 = new MockRule("Rule 5", "测试规则5");
        MockRule rule6 = new MockRule("Rule 6", "测试规则6");
        MockRule rule7 = new MockRule("Rule 7", "测试规则7");

        SerialRuleSet<String> rs1 = Rule.serial(
                rule1,
                rule2,
                rule3
        );
        RuleCheckResult result1 = rs1.check("pass");
        rs1.drawImageWithResult(result1, this.getRandomPath());

        RuleCheckResult result2 = rs1.check("error");
        rs1.drawImageWithResult(result2, this.getRandomPath());
    }

    private String getRandomPath() {
        Random random = new Random();
        return "../images/" + random.nextInt(Integer.MAX_VALUE) + ".png";
    }
}
