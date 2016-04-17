package org.mule.tools.maven.exchange;

public class MojoConstants {
    static final String DEFAULT_MULE_RUNTIME = "3.7";
    static final String DEFAULT_ANYPOINT_ENVIRONMENT = "Production";
    static final String DEFAULT_ANYPOINT_URI = "https://anypoint.mulesoft.com";
    static final String DEFAULT_DESCRIPTION_PLACEHOLDER = "Lorem ipsum dolor sit amet, " +
            "consectetur adipiscing elit. Donec dolor mauris, rutrum ut suscipit ac, rutrum ut " +
            "felis. Fusce dignissim vestibulum augue, ut eleifend tellus fringilla sed. Sed auctor " +
            "pharetra tortor nec ultrices. Praesent eget volutpat nunc, quis gravida dolor. Sed " +
            "tempus dignissim pharetra. Suspendisse potenti. Aenean sit amet massa convallis nunc " +
            "euismod molestie.";
    static final String MULE_RUNTIME_VERSION_PATTERN_MATCHER = "^([1-4]\\.[0-9]).*$";
    static final String MULE_DOWNLOAD_URL_PATTERN_MATCHER = "^(.*).mule$";
    static final String MULE_GITHUB_CONNECTION_URL_PATTERN_MATCHER = "^scm:git:(.*github\\.com.*)\\.git.*$";
    static final String DEFAULT_DESCRIPTION_FILE_SOURCE = "README.md";
    static final String DEFAULT_VERSIONING_STRATEGY = "incremental";
    static final String DEFAULT_EXCHANGE_API_VERSION = "v16";
}
