---

<div align="center">
    <b><em>Simple Rule</em></b><br>
    一个简单易用的规则验证库。
</div>

<div align="center">

[![MIT license](http://img.shields.io/badge/license-MIT-brightgreen.svg?style=flat)](http://opensource.org/licenses/MIT)

</div>

---

- [What is Simple Rule?](#what-is-simple-rule-)
- [Core Features](#core-features)
- [Quick Start](#quick-start)
  * [场景](#--)
  * [实施](#--)
- [Documentation](#documentation)
  * [创建规则](#----)
  * [返回检查结果](#------)
  * [组合规则](#----)
  * [规则集](#---)
  * [并行验证](#----)
  * [流程图](#---)

## What is Simple Rule?

在日常业务开发中，我们常常需要对业务实体进行规则校验。举个例子，在采购系统中，当创建一个新的采购订单时，我们需要对该订单进行以下验证：

- 采购人必须拥有采购权限。
- 采购总金额不能超过 10000 元。
- 采购商品必须是允许采购的。
- 采购商品种类不能超过 10 个。
- 采购商品数量不能超过 100 个。
- ……

同时，在不同的业务流程中，这些检查规则可能需要复用。

针对这种场景，我们自然希望把每个业务规则抽象为一个类，每个类实例代表一条业务规则。然后，对这些规则实例进行**组合**，最终得到完整的业务规则集。另外，当一个业务规则集变得非常复杂时，如果能够**可视化**地展示检查流程和检查结果，那么对于产品的用户体验和可维护性有非常大的帮助。

基于上述想法，我编写了 Simple Rule 这个库。

## Core Features

- 一个规则对应一个类，符合“高内聚、低耦合”的原则。
- 提供多种组合方式，满足各种规则组合需求。
- 支持并行检查，提高检测效率。
- 支持生成检测流程和检查结果的图片。

## Quick Start

### 场景

在订单创建流程中，我们需要对订单执行以下检查：

1. 购买人必须叫“Jake”。
2. 订单总金额必须大于 500。
3. 订单总金额必须小于 1000。
4. 订单创建日期必须是 2020 年内。

以上规则需从上至下依次执行，但第 2、3 条规则可以并行执行。

### 实施

1. 创建订单类：

```java
@AllArgsConstructor
@Getter
public class Order {
    private String buyer;
    private int amount;
    private LocalDateTime createTime;
}
```

2. 通过继承抽类 Rule<T>，创建 4 个规则子类：

```java
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
```

3. 组合规则，进行验证：

```java
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
        orderRules.drawImageWithResult(result, "images/orderRules.png");
    }
```

其中第 5 步导出的图片如下所示，其中绿色表示通过，红色表示未通过，灰色表示未执行。

<img src="./docs/images/orderRules.png" alt="orderRules" width="600"/>

## Documentation

### 创建规则

创建一个规则类主要有以下两步：

1. 继承`Rule<T>`抽象类，实现抽象方法`check(T target)`。这里的类型参数`T`代表待检查的目标类型。
2. 在类上添加注解`@Rule.Descriptor()`，提供该规则类的描述信息。

例：

```java
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
```

`@Rule.Descriptor()`注解的作用主要是指定规则类的`description`字段值。另外也可以用它来指定规则类的`name`字段值，如下：

```java
@Rule.Descriptor(name = "金额上限", value = "订单总金额必须小于1000")
public class AmountMustLessThan1000 extends Rule<Order> {
    ...
```

既然是字段值，那么也可以选择不使用注解，直接赋值：

```java
public class AmountMustLessThan1000 extends Rule<Order> {

    public AmountMustLessThan1000(){
        this.setName("金额上限");
        this.setDescription("订单总金额必须小于1000");
    }

    ...
}
```

不过推荐使用注解，因为后续可能会进一步扩展注解的功能。

注意，`name`字段需确保全局唯一。如果不主动指定，那么默认为当前规则类的类名。

### 返回检查结果

`check`方法的返回类型是`RuleCheckResult`。为了快速创建该对象，`Rule<T>`中提供了以下一些辅助方法：

**检查通过**：

```java
RuleCheckResult pass();
RuleCheckResult pass(String message);
RuleCheckResult pass(String message, Object payload);
```

**检查未通过**：

```java
RuleCheckResult error(String message);
RuleCheckResult error(String message, Object payload);
```

**产生警告**：

```java
RuleCheckResult warning(String message);
RuleCheckResult warning(String message, Object payload);
```

### 组合规则

有多种方式可以组合规则。

对于少量的规则，可以利用`Rule<T>`的`and()`和`or()`方法进行组合：

```java
MockRule ruleA = new MockRule();
MockRule ruleB = new MockRule();
SerialRuleSet<String> ruleC = ruleA.and(ruleB); // ruleC要求同时满足ruleA和ruleB
SerialRuleSet<String> ruleD = ruleA.or(ruleB); // ruleD要求满足ruleA或ruleB其中一个
```

注意，组合产生的`RuleSet`，本身也是`Rule`，因此可以继续组合。这采用了设计模式中的“组合模式”。

```java
MockRule ruleA = new MockRule();
MockRule ruleB = new MockRule();
MockRule ruleC = new MockRule();

SerialRuleSet<String> ruleD = ruleA.and(ruleB).and(ruleC); // ruleD需要同时满足三个规则
```

对于数量较多的规则，可以使用静态方法`Rule.serial()`和`Rule.parallel()`进行组合：

```java
MockRule ruleA = new MockRule();
MockRule ruleB = new MockRule();
MockRule ruleC = new MockRule();
MockRule ruleD = new MockRule();
SerialRuleSet<String> ruleE = Rule.serial(ruleA, ruleB, ruleC, ruleD); // 依次检查A、B、C、D四个规则，都通过才算通过。

ParallelRuleSet<String> ruleF = Rule.parallel(ruleA, ruleB, ruleC, ruleD); // 并行检查A、B、C、D四个规则，都通过才算通过。
```

默认情况下，`Rule.serial()`和`Rule.parallel()`组合各个规则使用的运算符是`and`，也就是要求所有规则都通过，组合后的规则集才算通过。可以通过传入不同的运算符来修改该行为：

```java
MockRule ruleA = new MockRule();
MockRule ruleB = new MockRule();
MockRule ruleC = new MockRule();
MockRule ruleD = new MockRule();
SerialRuleSet<String> ruleE = Rule.serial(
    RuleSet.Operator.OR,
    ruleA, ruleB, ruleC, ruleD); // 依次检查A、B、C、D四个规则，只要有一个通过就算通过。

ParallelRuleSet<String> ruleF = Rule.parallel(
    RuleSet.Operator.OR,
    ruleA, ruleB, ruleC, ruleD); // 并行检查A、B、C、D四个规则，只要有一个通过就算通过。
```

通过`Rule.serial()`和`Rule.parallel()`的嵌套使用，可以组合出复杂的规则集：

```java
SerialRuleSet<String> ruleSet = Rule.serial(
    rule1,
    Rule.parallel(
            Rule.parallel(
                    rule2,
                    Rule.serial(RuleSet.Operator.OR, rule3, rule4),
                    Rule.parallel(RuleSet.Operator.OR, rule5, rule6)),
            rule7
    )
```

### 规则集

对于需要反复使用的规则组合，可以直接创建一个规则集类，包含所有要使用的规则。待使用是只需要实例化该规则集类即可。

例：

```java
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

...

    public static void main(String[] args) throws IOException {

        // 1. 创建订单
        Order order = new Order("Jake", 1200, LocalDateTime.of(2019, 1, 1, 0, 0));

        // 2. 创建规则集
        OrderRuleSet orderRuleSet = new OrderRuleSet();

        // 3. 执行检查
        RuleCheckResult result = orderRuleSet.check(order);
    }
```

另外正如上面所述，任何`RuleSet`其实也是`Rule`，因此可以和别的规则任意组合。

### 并行验证

`ParallelRuleSet<T>`类用于支持并行验证，该规则集中包含的规则将并行地开启验证。

有三种方法可以创建一个`ParallelRuleSet<T>`实例：

1. 通过`ParallelRuleSet<T>`构造函数。
2. 通过`Rule.parallel()`静态方法。
3. 继承`ParallelRuleSet<T>`，创建一个单独的 RuleSet 类。

以第 1 种方法为例：

```java
    @Test
    public void testParallel() {
        // 定义一个规则，该规则检查将耗时约2秒。
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

        // 创建一个并行规则集
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

        // 测量规则集执行时间
        LocalDateTime start = LocalDateTime.now();
        RuleCheckResult result = ruleSet.checkAsync("Any").join();
        LocalDateTime end = LocalDateTime.now();
        long elapsed = Duration.between(start, end).toMillis();
        // 执行时间应低于3秒
        Assertions.assertTrue(elapsed < 3000);
    }
```

默认情况下，并发规则集使用`ForkJoinPool.commonPool()`线程池。也可以自定义线程池。不过需要显式地创建`RuleEngine`。

### 流程图

流程图有两种，一种是不带执行结果的，一种是带执行结果的。

**不带执行结果的流程图**通过调用`drawImage`方法生成：

```java
    // 创建规则集
    SerialRuleSet<String> ruleSet = Rule.serial(
                rule1,
                Rule.parallel(
                        Rule.parallel(rule2,
                                Rule.serial(rule3, rule4),
                                Rule.parallel(rule5, rule6)),
                        rule7
                )
        );
    // 生成流程图
    ruleSet.drawImage("images/不带执行结果的流程图.png");
```

结果如下：

<img src="./images/不带执行结果的流程图.png" alt="不带执行结果的流程图" width="600"/>

**带结果的流程图**则需要先得到执行结果，然后再将其作为参数调用`drawImageWithResult`：

```java
    rule4.setPassStr("Don't Pass"); // 让Rule4检查不通过，其他规则都可检查通过
    SerialRuleSet<String> ruleSet = Rule.serial(
            rule1,
            Rule.parallel(
                    Rule.parallel(rule2,
                            Rule.serial(rule3, rule4),
                            Rule.parallel(rule5, rule6)),
                    rule7
            )
    );
    RuleCheckResult result = ruleSet.check("pass");
    ruleSet.drawImageWithResult(result, "images/带执行结果的流程图.png");
```

结果如下：

<img src="./images/带执行结果的流程图.png" alt="带执行结果的流程图" width="600"/>
