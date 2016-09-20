package org.mule.tools.maven.exchange;

public class MojoConstants {
    public static final String DEFAULT_ANYPOINT_URI = "https://anypoint.mulesoft.com";
    public static final String DEFAULT_DESCRIPTION_PLACEHOLDER = "Lorem ipsum dolor sit amet, " +
            "consectetur adipiscing elit. Donec dolor mauris, rutrum ut suscipit ac, rutrum ut " +
            "felis. Fusce dignissim vestibulum augue, ut eleifend tellus fringilla sed. Sed auctor " +
            "pharetra tortor nec ultrices. Praesent eget volutpat nunc, quis gravida dolor. Sed " +
            "tempus dignissim pharetra. Suspendisse potenti. Aenean sit amet massa convallis nunc " +
            "euismod molestie.";
    public static final String MULE_RUNTIME_VERSION_PATTERN_MATCHER = "^([0-4]\\.[0-9]+).*$";
    public static final String MULE_DOWNLOAD_URL_PATTERN_MATCHER = "^(.*).mule$";
    public static final String GITHUB_CONNECTION_URL_PATTERN_MATCHER = "^scm:git:(.*github\\.com.*)\\.git.*$";
    public static final String CONNECTOR_FEATURE_RUNTIME_PATTERN_MATCHER = "^.*([0-9]\\.[0-9])\\.0$";
    public static final String CONNECTOR_VERSION_PATTERN_MATCHER = "^([0-9]+\\.[0-9]+\\.[0-9]+).*$";
    public static final String DEFAULT_DESCRIPTION_FILE_SOURCE = "README.md";
    public static final String DEFAULT_VERSIONING_STRATEGY = "incremental";
    public static final String DEFAULT_EXCHANGE_API_VERSION = "v16";
}
