package quickstart;

import com.github.watermoonlx.simpleRule.core.Rule;
import com.github.watermoonlx.simpleRule.core.SerialRuleSet;

@Rule.Descriptor("订单规则集")
public class OrderRuleSet extends SerialRuleSet<Order> {
    public OrderRuleSet() {
        // 组合规则
        SerialRuleSet<Order> ruleSet = Rule.serial(
                new BuyerMustBeJake(),
                Rule.parallel(
                        new AmountMustBiggerThan500(),
                        new AmountMustLessThan1000()
                ),
                new CreateTimeMustIn2020()
        );
        // 注册规则集
        this.register(ruleSet);
    }
}
