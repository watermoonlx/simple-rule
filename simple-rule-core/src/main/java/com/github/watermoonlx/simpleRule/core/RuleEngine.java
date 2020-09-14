package com.github.watermoonlx.simpleRule.core;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

@NoArgsConstructor
@AllArgsConstructor
public class RuleEngine {

    private Executor pool = ForkJoinPool.commonPool();

    public <T> RuleCheckResult run(@NonNull Rule<T> rule, @NonNull T target) {
        return runAsync(rule, target).join();
    }

    public <T> CompletableFuture<RuleCheckResult> runAsync(@NonNull Rule<T> rule, @NonNull T target) {
        return CompletableFuture.supplyAsync(() -> rule.check(target), this.pool);
    }

    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<RuleCheckResult> runAsync(@NonNull ParallelRuleSet<T> ruleSet, @NonNull T target) {
        CompletableFuture<RuleCheckResult>[] resultFutures = ruleSet.getSubRules()
                .stream()
                .map(r -> r.checkAsync(target, this))
                .toArray(CompletableFuture[]::new);
        CompletableFuture<RuleCheckResult> combinedFuture = CompletableFuture.allOf(resultFutures)
                .thenApply(i -> {
                    RuleCheckResult combinedResult = new RuleCheckResult();
                    for (CompletableFuture<RuleCheckResult> future : resultFutures) {
                        RuleCheckResult result = future.join();
                        combinedResult = combinedResult.combine(result);
                        if (ruleSet.getOperator() == RuleSet.Operator.OR) {
                            if (!result.hasError()) {
                                return result;
                            }
                        }
                    }
                    return combinedResult;
                });
        return combinedFuture;
    }

    public <T> CompletableFuture<RuleCheckResult> runAsync(@NonNull SerialRuleSet<T> ruleSet, @NonNull T target) {
        return CompletableFuture.supplyAsync(() -> {
            RuleCheckResult combinedResult = new RuleCheckResult();
            for (Rule<T> subRule : ruleSet.getSubRules()) {
                RuleCheckResult result = subRule.checkAsync(target, this).join();
                combinedResult = combinedResult.combine(result);
                if (ruleSet.getOperator() == RuleSet.Operator.AND) {
                    if (result.hasError()) {
                        return combinedResult;
                    }
                } else {
                    if (!result.hasError()) {
                        return result;
                    }
                }
            }
            return combinedResult;
        });
    }

    public void setThreadPool(@NonNull Executor executor) {
        this.pool = executor;
    }
}
