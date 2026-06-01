package messagerie.backend.config;

public final class CorsHeadersConfig {

    public static final String FRONTEND_DEV_LOCALHOST = "http://localhost:5173";
    public static final String FRONTEND_DEV_LOCALHOST_ALT = "http://localhost:3000";
    public static final String FRONTEND_PROD = "https://messagerie.example.com";

    public static final String BACKEND_URL = "http://localhost:8080";
    public static final String BACKEND_API_BASE = "/api";
    
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_X_REQUESTED_WITH = "X-Requested-With";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_ORIGIN = "Origin";
    public static final String HEADER_X_USER_ID = "X-User-Id";
    public static final String HEADER_X_CSRF_TOKEN = "X-CSRF-Token";
    public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    public static final String HEADER_CONTENT_LANGUAGE = "Content-Language";

    public static final String RESPONSE_HEADER_AUTHORIZATION = "Authorization";
    public static final String RESPONSE_HEADER_CONTENT_TYPE = "Content-Type";
    public static final String RESPONSE_HEADER_X_TOTAL_COUNT = "X-Total-Count";
    public static final String RESPONSE_HEADER_X_PAGE_NUMBER = "X-Page-Number";
    public static final String RESPONSE_HEADER_X_PAGE_SIZE = "X-Page-Size";
    public static final String RESPONSE_HEADER_X_AUTH_TOKEN = "X-Auth-Token";

    public static final String[] ALLOWED_METHODS = {
        "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
    };

    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";

    public static final String TOKEN_TYPE_BEARER = "Bearer";
    public static final String BEARER_PREFIX = "Bearer ";

    public static final long PREFLIGHT_MAX_AGE = 3600L;
    public static final boolean ALLOW_CREDENTIALS = true;

    private CorsHeadersConfig() {
    }
}
