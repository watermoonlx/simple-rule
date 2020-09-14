package quickstart;

import com.github.watermoonlx.simpleRule.core.Rule;
import com.github.watermoonlx.simpleRule.core.RuleCheckResult;

@Rule.Descriptor("订单总金额必须大于500")
public class AmountMustBiggerThan500 extends Rule<Order> {
    @Override
    public RuleCheckResult check(Order target) {
        if (target.getAmount() > 500) {
            return this.pass("通过");
        } else {
            return this.error(String.format("订单总金额必须大于500，当前金额为：%d。", target.getAmount()));
        }
    }
}
