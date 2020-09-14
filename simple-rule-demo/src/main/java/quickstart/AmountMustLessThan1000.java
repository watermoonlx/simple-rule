package quickstart;

import com.github.watermoonlx.simpleRule.core.Rule;
import com.github.watermoonlx.simpleRule.core.RuleCheckResult;

@Rule.Descriptor("订单总金额必须小于1000")
public class AmountMustLessThan1000 extends Rule<Order> {
    @Override
    public RuleCheckResult check(Order target) {
        if (target.getAmount() < 1000) {
            return this.pass();
        } else {
            return this.error(String.format("订单总金额必须小于1000，当前金额为：%d。", target.getAmount()));
        }
    }
}
