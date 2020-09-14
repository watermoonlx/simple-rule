package com.github.watermoonlx.simpleRule.utils;

import com.github.watermoonlx.simpleRule.core.RuleCheckResult;
import com.github.watermoonlx.simpleRule.core.RuleCheckResultDetail;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;

public class AssertUtils {
    public static void assertResultEquals(RuleCheckResult expected, RuleCheckResult actual) {
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(expected);

        assertAssertResultDetailsEquals(expected.getErrors(), actual.getErrors());
        assertAssertResultDetailsEquals(expected.getWarnings(), actual.getWarnings());
        assertAssertResultDetailsEquals(expected.getPasseds(), actual.getPasseds());
        assertAssertResultDetailsEquals(expected.getResolveds(), actual.getResolveds());
    }

    public static void assertResultDetailEquals(RuleCheckResultDetail expected, RuleCheckResultDetail actual) {
        Assertions.assertEquals(expected.getRuleName(), actual.getRuleName());
        Assertions.assertEquals(expected.getMessage(), actual.getMessage());
    }

    private static void assertAssertResultDetailsEquals(ArrayList<RuleCheckResultDetail> expected, ArrayList<RuleCheckResultDetail> actual) {
        Assertions.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            assertResultDetailEquals(expected.get(i), actual.get(i));
        }
    }
}
