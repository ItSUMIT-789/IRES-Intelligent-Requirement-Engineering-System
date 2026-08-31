package com.ires.user.entity;

import java.util.EnumSet;
import java.util.Set;

public enum RoleName {
    ADMIN,
    BUSINESS_ANALYST,
    CLIENT,
    DEVELOPER,
    TESTER;

    private static final Set<RoleName> PUBLIC_REGISTRATION_ROLES = EnumSet.of(
            BUSINESS_ANALYST,
            CLIENT,
            DEVELOPER,
            TESTER
    );

    public boolean canBeSelfRegistered() {
        return PUBLIC_REGISTRATION_ROLES.contains(this);
    }
}
