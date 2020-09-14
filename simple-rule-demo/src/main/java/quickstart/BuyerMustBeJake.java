package quickstart;

import com.github.watermoonlx.simpleRule.core.Rule;
import com.github.watermoonlx.simpleRule.core.RuleCheckResult;

@Rule.Descriptor("购买人必须是Jake")
public class BuyerMustBeJake extends Rule<Order> {
    @Override
    public RuleCheckResult check(Order target) {
        if (target.getBuyer().equalsIgnoreCase("Jake")) {
            return this.pass("通过");
        }else {
            return this.error(String.format("购买人必须是Jake，当前购买人：%s。", target.getBuyer()));
        }
    }
}
