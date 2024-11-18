package org.iam.convertor;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ListStringConverter {
    private static final String SPLIT_CHAR = ",";

    // List转字符串
    public static String listToString(List<Long> list) {
        if (list == null) {
            return null;
        }
        if (list.isEmpty()) {
            return "";
        }
        return list.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(SPLIT_CHAR));
    }

    // 字符串转List
    public static List<Long> stringToList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(str.split(SPLIT_CHAR))
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }
}