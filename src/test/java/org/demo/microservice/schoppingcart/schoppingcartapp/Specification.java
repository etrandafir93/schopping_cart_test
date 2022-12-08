package org.demo.microservice.schoppingcart.schoppingcartapp;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Specification {
    private static final int HEADER_ROWS = 2;
    private static final String NOT_APPLICABLE = "n/a";

    private final List<Row> specifications;

    public static Specification from(List<String> rows) {
        var headers = splitAndTrim(rows.get(0));
        var specs = rows.stream()
            .skip(HEADER_ROWS)
            .map(str -> splitAndTrim(str))
            .map(args -> Row.of(headers, args))
            .collect(toList());
        return new Specification(specs);
    }

    public Stream<Row> stream() {
        return specifications.stream();
    }

    private static List<String> splitAndTrim(String str) {
        return Arrays.stream(str.split("\\|")).map(String::trim)
            .collect(toList());
    }

    @Value
    public static class Row {
        Map<String, String> data;

        static Row of(List<String> keys, List<String> values) {
            Map<String, String> result = new HashMap<>();
            for (int i = 0; i < keys.size(); i++) {
                result.put(keys.get(i), values.get(i));
            }
            return new Row(result);
        }

        public String get(String key) {
            return data.get(key);
        }

        public Optional<String> getIfApplicable(String key) {
            String value = data.get(key);
            if (NOT_APPLICABLE.equalsIgnoreCase(value)) {
                return Optional.empty();
            }
            return Optional.ofNullable(value);
        }

        public BigDecimal getBigDecimal(String key) {
            return new BigDecimal(get(key));
        }
    }
}