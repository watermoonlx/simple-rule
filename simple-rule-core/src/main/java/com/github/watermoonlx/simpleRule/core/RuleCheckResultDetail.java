package com.github.watermoonlx.simpleRule.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RuleCheckResultDetail {
    /**
     * 产生该结果的规则名称
     */
    private String ruleName;

    /**
     * 检查结果信息
     */
    private String message;

    /**
     * 负载
     */
    private Object payload;

    public RuleCheckResultDetail(String ruleName, String message) {
        this(ruleName, message, null);
    }

    public boolean isFrom(String ruleName) {
        return this.ruleName.equalsIgnoreCase(ruleName);
    }

    public boolean isFrom(Class<? extends Rule> cls) {
        return this.isFrom(cls.getSimpleName());
    }

    @SuppressWarnings("unchecked")
    public <TPayload> TPayload getPayload() {
        return (TPayload) this.payload;
    }
}
