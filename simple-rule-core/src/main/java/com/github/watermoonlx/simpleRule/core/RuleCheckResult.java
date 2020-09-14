package com.github.watermoonlx.simpleRule.core;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Getter
public class RuleCheckResult {
    /**
     * 错误
     */
    private final ArrayList<RuleCheckResultDetail> errors = new ArrayList<>();

    /**
     * 警告
     */
    private final ArrayList<RuleCheckResultDetail> warnings = new ArrayList<>();

    /**
     * 已通过
     */
    private final ArrayList<RuleCheckResultDetail> passeds = new ArrayList<>();

    /**
     * 已解决的警告
     */
    private final ArrayList<RuleCheckResultDetail> resolveds = new ArrayList<>();


    public RuleCheckResult addError(RuleCheckResultDetail error) {
        this.errors.add(error);
        return this;
    }

    public RuleCheckResult addErrors(RuleCheckResultDetail... errors) {
        this.errors.addAll(Arrays.asList(errors));
        return this;
    }

    public RuleCheckResult addWarning(RuleCheckResultDetail warning) {
        this.warnings.add(warning);
        return this;
    }

    public RuleCheckResult addWarnings(RuleCheckResultDetail... warnings) {
        this.warnings.addAll(Arrays.asList(warnings));
        return this;
    }

    public RuleCheckResult addPassed(RuleCheckResultDetail passedInfo) {
        this.passeds.add(passedInfo);
        return this;
    }

    public RuleCheckResult addPasseds(RuleCheckResultDetail... passedInfo) {
        this.passeds.addAll(Arrays.asList(passedInfo));
        return this;
    }

    public boolean hasError() {
        return !this.errors.isEmpty();
    }

    public boolean hasError(String ruleName) {
        return this.hasDetailFrom(this.errors, ruleName);
    }

    public boolean hasError(Class<? extends Rule> cls) {
        return this.hasError(cls.getSimpleName());
    }

    public boolean hasWarning() {
        return !this.warnings.isEmpty();
    }

    public boolean hasWarning(String ruleName) {
        return this.hasDetailFrom(this.warnings, ruleName);
    }

    public boolean hasWarning(Class<? extends Rule> cls) {
        return this.hasWarning(cls.getSimpleName());
    }

    public RuleCheckResultDetail getError(String ruleName) {
        return this.getDetail(this.errors, ruleName);
    }

    public RuleCheckResultDetail getError(Class<? extends Rule> cls) {
        return this.getError(cls.getSimpleName());
    }

    public RuleCheckResultDetail getWarning(String ruleName) {
        return this.getDetail(this.warnings, ruleName);
    }

    public RuleCheckResultDetail getWarning(Class<? extends Rule> cls) {
        return this.getWarning(cls.getSimpleName());
    }

    public void resolveWarning(RuleCheckResultDetail warning) {
        if (this.warnings.remove(warning)) {
            this.resolveds.add(warning);
        }
    }

    public void resolveWarning(String ruleName) {
        RuleCheckResultDetail warning = this.getWarning(ruleName);
        this.resolveWarning(warning);
    }

    public void resolveWarning(Class<? extends Rule> cls) {
        RuleCheckResultDetail warning = this.getWarning(cls);
        this.resolveWarning(warning);
    }

    public void resolveAllWarnings() {
        if (this.warnings.isEmpty())
            return;

        Iterator<RuleCheckResultDetail> iterator = this.warnings.iterator();
        while (iterator.hasNext()) {
            this.resolveds.add(iterator.next());
            iterator.remove();
        }
    }

    /**
     * 该方法将返回一个新的RuleCheckResult，不会修改结合的两个对象本身。
     */
    public RuleCheckResult combine(RuleCheckResult other) {
        RuleCheckResult total = new RuleCheckResult();

        total.errors.addAll(this.errors);
        total.warnings.addAll(this.warnings);
        total.passeds.addAll(this.passeds);
        total.resolveds.addAll(this.resolveds);

        if (other != null) {
            total.errors.addAll(other.errors);
            total.warnings.addAll(other.warnings);
            total.passeds.addAll(other.passeds);
            total.resolveds.addAll(other.resolveds);
        }

        return total;
    }

    private boolean hasDetailFrom(List<RuleCheckResultDetail> details, String ruleName) {
        for (RuleCheckResultDetail detail : details) {
            if (detail.isFrom(ruleName)) {
                return true;
            }
        }
        return false;
    }

    private RuleCheckResultDetail getDetail(List<RuleCheckResultDetail> details, String ruleName) {
        for (RuleCheckResultDetail detail : details) {
            if (detail.isFrom(ruleName)) {
                return detail;
            }
        }
        return null;
    }

}
