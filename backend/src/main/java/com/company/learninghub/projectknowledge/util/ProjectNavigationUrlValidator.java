package com.company.learninghub.projectknowledge.util;

import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

public final class ProjectNavigationUrlValidator {

    private static final Pattern EMBEDDED_CREDENTIALS = Pattern.compile("^[^/:@]+:[^/@]+@");

    private ProjectNavigationUrlValidator() {
    }

    public static String normalizeNavigationUrl(String value, String fieldLabel) {
        String url = value == null ? "" : value.trim();
        if (!StringUtils.hasText(url)) {
            throw new IllegalArgumentException(fieldLabel + " is required");
        }
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            throw new IllegalArgumentException(fieldLabel + " must start with http:// or https://");
        }
        rejectEmbeddedCredentials(url, fieldLabel);
        return url;
    }

    public static void rejectEmbeddedCredentials(String url, String fieldLabel) {
        try {
            URI uri = new URI(url);
            String userInfo = uri.getUserInfo();
            if (StringUtils.hasText(userInfo)) {
                throw new IllegalArgumentException(fieldLabel + " must not contain embedded credentials");
            }
            String authority = uri.getAuthority();
            if (authority != null && EMBEDDED_CREDENTIALS.matcher(authority).find()) {
                throw new IllegalArgumentException(fieldLabel + " must not contain embedded credentials");
            }
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(fieldLabel + " is not a valid URL");
        }
    }
}
