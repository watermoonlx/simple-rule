package com.github.watermoonlx.simpleRule.core;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.CompletableFuture;

/**
 * 规则的抽象基类。
 * 设计模式：规约模式（Specification）和通知模式（Notification）的一种扩展。请参考：https://www.codeproject.com/Tips/790758/Specification-and-Notification-Patterns
 * 规则通常代表一种约束，我们通常将其称作业务规则。比如：“Buyer必须是PM”，就是一种规则。
 * 各种具体的业务规则，通过继承并实现该基类来定义，从而将各个规则隔离开，方便复用，比如上面的规则，需要创建一个Rule类：BuyerMustBePmRule。
 * 在实现具体规则类时，只需要重写Check(T target)这个方法。该方法接收验证目标（即类型参数T的一个实例）
 *
 * @param <T> 待检测的目标类型
 */
@Getter
@Setter
public abstract class Rule<T> {

    private String name;
    private String description;
    private final ImageGenerator imageGenerator = new ImageGenerator();

    {
        this.init();
    }

    public abstract RuleCheckResult check(T target);

    public CompletableFuture<RuleCheckResult> checkAsync(T target) {
        return this.checkAsync(target, new RuleEngine());
    }

    public CompletableFuture<RuleCheckResult> checkAsync(T target, @NonNull RuleEngine engine) {
        return engine.runAsync(this, target);
    }

    @SuppressWarnings("unchecked")
    public SerialRuleSet<T> and(@NonNull Rule<T> other) {
        return new SerialRuleSet<>(this, other);
    }

    @SuppressWarnings("unchecked")
    public SerialRuleSet<T> or(@NonNull Rule<T> other) {
        return new SerialRuleSet<>(RuleSet.Operator.OR, this, other);
    }

    public static <U> SerialRuleSet<U> serial(Rule<U>... rules) {
        return new SerialRuleSet<>(rules);
    }

    public static <U> SerialRuleSet<U> serial(RuleSet.Operator op, Rule<U>... rules) {
        return new SerialRuleSet<>(op, rules);
    }

    public static <U> ParallelRuleSet<U> parallel(Rule<U>... rules) {
        return new ParallelRuleSet<>(rules);
    }

    public static <U> ParallelRuleSet<U> parallel(RuleSet.Operator op, Rule<U>... rules) {
        return new ParallelRuleSet<>(op, rules);
    }

    //region 辅助构建RuleCheckResult的方法
    protected RuleCheckResult error(String message) {
        RuleCheckResultDetail error = new RuleCheckResultDetail(this.name, message);
        return this.customError(error);
    }

    protected RuleCheckResult error(String message, Object payload) {
        RuleCheckResultDetail error = new RuleCheckResultDetail(this.name, message, payload);
        return this.customError(error);
    }

    protected RuleCheckResult warning(String message) {
        RuleCheckResultDetail warning = new RuleCheckResultDetail(this.name, message);
        return this.customWarning(warning);
    }

    protected RuleCheckResult warning(String message, Object payload) {
        RuleCheckResultDetail warning = new RuleCheckResultDetail(this.name, message, payload);
        return this.customWarning(warning);
    }

    protected RuleCheckResult pass() {
        return this.pass(null);
    }

    protected RuleCheckResult pass(String message) {
        RuleCheckResultDetail passInfo = new RuleCheckResultDetail(this.name, message);
        return this.customPassInfo(passInfo);
    }

    protected RuleCheckResult pass(String message, Object payload) {
        RuleCheckResultDetail passInfo = new RuleCheckResultDetail(this.name, message, payload);
        return this.customPassInfo(passInfo);
    }

    protected RuleCheckResult customError(RuleCheckResultDetail error) {
        this.setRuleNameIfEmpty(error);
        RuleCheckResult result = new RuleCheckResult();
        result.addError(error);
        return result;
    }

    protected RuleCheckResult customWarning(RuleCheckResultDetail warning) {
        this.setRuleNameIfEmpty(warning);
        RuleCheckResult result = new RuleCheckResult();
        result.addWarning(warning);
        return result;
    }

    protected RuleCheckResult customPassInfo(RuleCheckResultDetail passedInfo) {
        this.setRuleNameIfEmpty(passedInfo);
        RuleCheckResult result = new RuleCheckResult();
        result.addPassed(passedInfo);
        return result;
    }


    private void setRuleNameIfEmpty(RuleCheckResultDetail detail) {
        if (detail.getRuleName() == null || detail.getRuleName().isEmpty()) {
            detail.setRuleName(this.name);
        }
    }
    //endregion

    //region 导出图片
    public void drawImage(@NonNull OutputStream outputStream) throws IOException {
        this.imageGenerator.generate(this, outputStream);
    }

    public void drawImage(@NonNull String filePath) throws IOException {
        this.imageGenerator.generate(this, filePath);
    }

    public void drawImageWithResult(@NonNull RuleCheckResult result, @NonNull OutputStream outputStream) throws IOException {
        this.imageGenerator.generateWithResult(this, result, outputStream);
    }

    public void drawImageWithResult(@NonNull RuleCheckResult result, @NonNull String filePath) throws IOException {
        this.imageGenerator.generateWithResult(this, result, filePath);
    }
    //endregion

    private void init() {
        Descriptor desc = this.getClass().getAnnotation(Descriptor.class);
        if (desc != null) {
            this.description = desc.value();
            this.name = desc.name();
        }
        if (this.name == null || this.name.isEmpty()) {
            this.name = this.getClass().getSimpleName();
        }
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Descriptor {
        String name() default "";

        String value();
    }
}
