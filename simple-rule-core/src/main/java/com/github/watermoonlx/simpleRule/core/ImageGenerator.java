package com.github.watermoonlx.simpleRule.core;

import javafx.util.Pair;
import lombok.NonNull;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

class ImageGenerator {

    private static final String PASS_COLOR = "#GreenYellow";
    private static final String ERROR_COLOR = "#ff4d4f";
    private static final String WARNING_COLOR = "#faad14";
    private static final String NOT_RUN_COLOR = "#d9d9d9";

    private HashMap<String, Pair<String, RuleCheckResultDetail>> resultMap;

    public void generate(@NonNull Rule<?> rule, @NonNull OutputStream outputStream) throws IOException {
        String diagramDescription = this.getDiagramDescription(rule);
        SourceStringReader reader = new SourceStringReader(diagramDescription);
        reader.generateImage(outputStream);
    }

    public void generate(@NonNull Rule<?> rule, @NonNull String filePath) throws IOException {
        Path absPath = Paths.get(filePath).toAbsolutePath();
        if (!Files.exists(absPath)) {
            Files.createFile(absPath);
        }
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath))) {
            this.generate(rule, outputStream);
        }
    }

    public void generateWithResult(@NonNull Rule<?> rule, @NonNull RuleCheckResult result, @NonNull OutputStream outputStream) throws IOException {
        try {
            this.initResultMap(result);
            this.generate(rule, outputStream);
        } finally {
            this.resultMap = null;
        }
    }

    public void generateWithResult(@NonNull Rule<?> rule, @NonNull RuleCheckResult result, @NonNull String filePath) throws IOException {
        try {
            this.initResultMap(result);
            this.generate(rule, filePath);
        } finally {
            this.resultMap = null;
        }
    }

    private void initResultMap(@NonNull RuleCheckResult result) {
        this.resultMap = new HashMap<>();
        for (RuleCheckResultDetail r : result.getPasseds()) {
            this.resultMap.put(r.getRuleName(), new Pair<>(PASS_COLOR, r));
        }
        for (RuleCheckResultDetail r : result.getErrors()) {
            this.resultMap.put(r.getRuleName(), new Pair<>(ERROR_COLOR, r));
        }
        for (RuleCheckResultDetail r : result.getWarnings()) {
            this.resultMap.put(r.getRuleName(), new Pair<>(WARNING_COLOR, r));
        }
        for (RuleCheckResultDetail r : result.getResolveds()) {
            this.resultMap.put(r.getRuleName(), new Pair<>(WARNING_COLOR, r));
        }
    }

    private String getDiagramDescription(@NonNull Rule<?> rule) {
        StringBuilder sb = new StringBuilder();
        sb.append("@startuml\n");
        sb.append("scale 800*600\n");
        sb.append("start\n");

        this.getDiagramDescriptionBody(rule, null, sb);

        sb.append("end\n");
        sb.append("@enduml\n");

        return sb.toString();
    }

    private void getDiagramDescriptionBody(@NonNull Rule<?> rule, RuleSet.Operator parentOp, StringBuilder sb) {
        if (rule instanceof SerialRuleSet) {
            SerialRuleSet<?> ruleSet = (SerialRuleSet<?>) rule;
            if (ruleSet.getOperator() == RuleSet.Operator.OR && parentOp != RuleSet.Operator.OR) {
                sb.append("partition 满足任意一个即可 {\n");
            }
            for (Rule<?> subRule : ruleSet.getSubRules()) {
                this.getDiagramDescriptionBody(subRule, ruleSet.getOperator(), sb);
            }
            if (ruleSet.getOperator() == RuleSet.Operator.OR && parentOp != RuleSet.Operator.OR) {
                sb.append("}\n");
            }
        } else if (rule instanceof ParallelRuleSet) {
            ParallelRuleSet<?> ruleSet = (ParallelRuleSet<?>) rule;
            if (ruleSet.getOperator() == RuleSet.Operator.OR) {
                sb.append("partition 满足任意一个即可 {\n");
            }
            int count = 0;
            for (Rule<?> subRule : ruleSet.getSubRules()) {
                if (count == 0) {
                    sb.append("fork\n");
                } else {
                    sb.append("fork again\n");
                }
                this.getDiagramDescriptionBody(subRule, ruleSet.getOperator(), sb);
                count++;
            }
            sb.append("end fork\n");
            if (ruleSet.getOperator() == RuleSet.Operator.OR) {
                sb.append("}\n");
            }
        } else {
            if (this.resultMap == null) {
                sb.append(":=== ").append(rule.getName());
                if (rule.getDescription() != null && !rule.getDescription().isEmpty()) {
                    sb.append("\n").append(rule.getDescription());
                }
            } else {
                Pair<String, RuleCheckResultDetail> colorAndDetail = this.resultMap.getOrDefault(rule.getName(), new Pair<>(NOT_RUN_COLOR, null));
                String color = colorAndDetail.getKey();
                RuleCheckResultDetail detail = colorAndDetail.getValue();
                sb.append(color).append(":=== ").append(rule.getName());
                if (rule.getDescription() != null && !rule.getDescription().isEmpty()) {
                    sb.append("\n").append(rule.getDescription());
                }
                if (detail != null && detail.getMessage() != null) {
                    sb.append("\n----\n").append("**结果：**").append(detail.getMessage());
                }
            }
            sb.append(";\n");
        }
    }

}
