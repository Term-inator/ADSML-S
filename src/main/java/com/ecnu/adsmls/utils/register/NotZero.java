package com.ecnu.adsmls.utils.register;

import java.util.Map;

public class NotZero implements Requirement {
    public NotZero() {
    }

    @Override
    public boolean check(Map<String, String> context, String value) {
        return Double.parseDouble(value) != 0;
    }
}
