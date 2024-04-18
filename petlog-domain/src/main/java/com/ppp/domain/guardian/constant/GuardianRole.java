package com.ppp.domain.guardian.constant;

public enum GuardianRole {
    MEMBER("member"),
    LEADER("leader");

    private final String value;

    GuardianRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
