package com.example.playmatch.auth.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Represents available gender options in the system.
 * Maps to the gender_enum PostgreSQL ENUM type.
 */
@Getter
public enum Gender {
    MALE("male"),
    FEMALE("female"),
    OTHER("other"),
    PREFER_NOT_TO_SAY("prefer_not_to_say");

    private final String value;

    Gender(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Gender fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Gender value cannot be null");
        }

        for (Gender g : Gender.values()) {
            if (g.value.equalsIgnoreCase(value)) {
                return g;
            }
        }
        throw new IllegalArgumentException("Unknown gender: " + value);
    }
}
