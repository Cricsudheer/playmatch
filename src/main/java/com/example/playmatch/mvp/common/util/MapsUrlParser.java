package com.example.playmatch.mvp.common.util;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MapsUrlParser {
    private static final Pattern LAT_LNG_PATTERN = Pattern.compile("@(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)");

    public static Double[] parse(String mapsUrl) {
        if (mapsUrl == null || mapsUrl.isBlank()) {
            return new Double[]{null, null};
        }

        try {
            Matcher matcher = LAT_LNG_PATTERN.matcher(mapsUrl);
            if (matcher.find()) {
                double lat = Double.parseDouble(matcher.group(1));
                double lng = Double.parseDouble(matcher.group(2));
                log.debug("Parsed coordinates from {}: lat={}, lng={}", mapsUrl, lat, lng);
                return new Double[]{lat, lng};
            }
        } catch (Exception e) {
            log.warn("Failed to parse coordinates from maps URL: {}", mapsUrl, e);
        }

        log.debug("No coordinates found in maps URL: {}", mapsUrl);
        return new Double[]{null, null};
    }
}
