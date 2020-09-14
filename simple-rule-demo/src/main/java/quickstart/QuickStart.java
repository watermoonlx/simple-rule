package quickstart;

import com.github.watermoonlx.simpleRule.core.Rule;
import com.github.watermoonlx.simpleRule.core.RuleCheckResult;
import com.github.watermoonlx.simpleRule.core.SerialRuleSet;

import java.io.IOException;
import java.time.LocalDateTime;

public class QuickStart {
    public static void main(String[] args) throws IOException {

        // 1. 创建订单
        Order order = new Order("Jake", 1200, LocalDateTime.of(2019, 1, 1, 0, 0));

        // 2. 通过组合各规则对象，构建订单规则集。
        SerialRuleSet<Order> orderRules = Rule.serial(
                new BuyerMustBeJake(),
                Rule.parallel(
                        new AmountMustBiggerThan500(),
                        new AmountMustLessThan1000()
                ),
                new CreateTimeMustIn2020()
        );

        // 3. 使用订单规则集检查订单，获得检查结果
        RuleCheckResult result = orderRules.check(order);

        // 4. 判断结果中是否有错误
        if (result.hasError()) {
            System.out.println("检查未通过"); // 控制台打印出：检查未通过
        }

        // 5. 导出检查流程图片
        orderRules.drawImageWithResult(result, "docs/images/orderRules.png");
    }
}
