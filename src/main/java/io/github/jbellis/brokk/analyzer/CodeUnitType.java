package io.github.jbellis.brokk.analyzer;

import java.util.EnumSet;
import java.util.Set;

public enum CodeUnitType {
    CLASS,
    FIELD,
    FUNCTION;

    public static final Set<CodeUnitType> ALL = EnumSet.of(CLASS, FIELD, FUNCTION);
}
