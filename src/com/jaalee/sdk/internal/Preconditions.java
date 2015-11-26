package com.jaalee.sdk.internal;

/**
 * @author JAALEE, Inc
 * @Support dev@jaalee.com
 * @Sales: sales@jaalee.com
 * @see http://www.jaalee.com/
 */

public class Preconditions {
    public static <T> T checkNotNull(T reference, Object errorMessage) {
        if (reference == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
        return reference;
    }

    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    public static void checkArgument(boolean expression) {
        if (!expression)
            throw new IllegalArgumentException();
    }

    public static void checkArgument(boolean expression, Object errorMessage) {
        if (!expression)
            throw new IllegalArgumentException(String.valueOf(errorMessage));
    }

    public static void checkArgument(boolean expression, String errorMessageTemplate, Object[] errorMessageArgs) {
        if (!expression)
            throw new IllegalArgumentException(format(errorMessageTemplate, errorMessageArgs));
    }

    static String format(String template, Object[] args) {
        template = String.valueOf(template);

        StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);

        int templateStart = 0;
        int i = 0;
        while (i < args.length) {
            int placeholderStart = template.indexOf("%s", templateStart);
            if (placeholderStart == -1) {
                break;
            }
            builder.append(template.substring(templateStart, placeholderStart));
            builder.append(args[(i++)]);
            templateStart = placeholderStart + 2;
        }
        builder.append(template.substring(templateStart));

        if (i < args.length) {
            builder.append(" [");
            builder.append(args[(i++)]);
            while (i < args.length) {
                builder.append(", ");
                builder.append(args[(i++)]);
            }
            builder.append(']');
        }

        return builder.toString();
    }
}
