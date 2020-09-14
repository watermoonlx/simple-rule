package quickstart;


import com.github.watermoonlx.simpleRule.core.Rule;
import com.github.watermoonlx.simpleRule.core.RuleCheckResult;

@Rule.Descriptor("订单创建日期必须是2020年内")
public class CreateTimeMustIn2020 extends Rule<Order> {
    @Override
    public RuleCheckResult check(Order target) {
        if (target.getCreateTime().getYear() == 2020) {
            return this.pass("通过");
        } else {
            return this.error(String.format("订单创建日期必须是2020年内，当前订单创建年份：%d.", target.getCreateTime().getYear()));
        }
    }
}
