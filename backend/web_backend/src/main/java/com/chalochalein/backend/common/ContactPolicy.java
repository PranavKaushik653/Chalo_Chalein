package com.chalochalein.backend.common;

import java.util.regex.Pattern;

public final class ContactPolicy {

    private static final Pattern URL    = Pattern.compile("(?i)\\b(?:https?://|www\\.)\\S+");
    private static final Pattern HANDLE = Pattern.compile("[A-Za-z0-9._-]{2,}@[A-Za-z]{2,}");
    private static final Pattern INSTA  = Pattern.compile("(?<!\\w)@[A-Za-z0-9._]{3,}");
    private static final Pattern PHONE  = Pattern.compile("(?<!\\d)(?:\\+?91[\\s-]?)?[6-9]\\d{4}[\\s-]?\\d{5}(?!\\d)");

    private ContactPolicy() {}

    public static boolean hasContactOrLink(String s){
        return s != null && (URL.matcher(s).find() || HANDLE.matcher(s).find() ||
                INSTA.matcher(s).find() || PHONE.matcher(s).find());
    }
    public static String redact(String s) {
        String r = URL.matcher(s).replaceAll("[link removed]");
        r = HANDLE.matcher(r).replaceAll("[contact removed]");
        r = INSTA.matcher(r).replaceAll("[contact removed]");
        return PHONE.matcher(r).replaceAll("[number removed]");
    }

    /** Drops control characters; optionally flattens newlines. */
    public static String clean(String s, boolean singleLine) {
        if (s == null) return null;
        String r = s.replaceAll("[\\p{Cntrl}&&[^\\n\\t]]", "");
        if (singleLine) r = r.replaceAll("[\\n\\t]+", " ");
        return r.strip();
    }
}
