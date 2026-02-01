/*
 * Copyright 2021 The exTHmUI Open Source Project
 * Copyright (C) 2025 The AviumUI Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.avium.sms;

import android.text.TextUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 短信验证码提取
 * 感谢exthmui的正则表达式与代码思路
 */
public class VerificationCodeUtil {

    private static final String[] KEYWORDS = {
        "code", "verification", "otp", "验证码", "校验码", "检验码", "确认码", "激活码",
        "动态码", "动态密码", "交易码", "上网码", "认证码", "识别码", "短信口令",
        "验证", "校验", "检验", "激活", "交易", "认证", "识别"
    };

    private static final Pattern CODE_PATTERN = Pattern.compile(
        "(?<!\\d)(\\d{4,8})(?!\\d)"
    );

    private static final Pattern STRICT_CODE_PATTERN = Pattern.compile(
        "(?:验证码|校验码|检验码|确认码|激活码|动态码|动态密码|交易码|上网码|认证码|识别码|code|verification|otp)[^\\d]{0,10}([\\d]{4,8})"
    );

    private static final Pattern EXCLUDE_PATTERN = Pattern.compile(
        "(\\d{2}[-/]\\d{2}[-/]\\d{2,4})|(\\d{2}:\\d{2}(:\\d{2})?)|([\\d]{11,})"
    );

    public static String extractVerificationCode(String messageBody) {
        if (TextUtils.isEmpty(messageBody)) {
            return null;
        }

        boolean hasKeyword = false;
        String lowerBody = messageBody.toLowerCase();
        for (String keyword : KEYWORDS) {
            if (lowerBody.contains(keyword)) {
                hasKeyword = true;
                break;
            }
        }
        if (!hasKeyword) {
            return null;
        }

        Matcher strictMatcher = STRICT_CODE_PATTERN.matcher(messageBody);
        if (strictMatcher.find()) {
            return strictMatcher.group(1);
        }

        Matcher matcher = CODE_PATTERN.matcher(messageBody);
        while (matcher.find()) {
            String code = matcher.group(1);
            int start = matcher.start();
            int end = matcher.end();

            boolean isExcluded = false;
            Matcher excludeMatcher = EXCLUDE_PATTERN.matcher(messageBody);
            while (excludeMatcher.find()) {
                if (excludeMatcher.start() <= start && excludeMatcher.end() >= end) {
                    isExcluded = true;
                    break;
                }
            }

            if (!isExcluded) {
                return code;
            }
        }

        return null;
    }
}