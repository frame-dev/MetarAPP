package ch.framedev.metarapp.util;

public enum ErrorCode {

    ERROR_404("x404", 404),
    ERROR_AIRPORT_NOT_FOUND("x445", 445),
    ERROR_LOAD("x500", 500),
    ERROR_API_DOWN("x501",501),
    ERROR_INVALID_REQUEST("x502",502),
    ERROR_DOWNLOAD("x503", 503),
    ERROR_JSON_SAVE("x504", 504),
    ERROR_JSON_LOAD("x505", 505),
    ERROR_JSON_PARSE("x506", 506),
    ERROR_FILE_NOT_FOUND("x507", 507),
    ERROR_UPDATE("x245", 245),
    ERROR_CREATE_FILE_OR_DIRECTORY("x246", 246),
    ERROR_REMOVE_FILE_OR_DIRECTORY("x247", 247),
    ERROR_LOCALE_NOT_FOUND("x248", 248),
    ERROR_SAVE("x249", 249),
    ERROR_NULL_OBJECT("x250", 250),
    ERROR_OPEN_LINK("x251", 250),
    ERROR_NO_NETWORK("x252", 251),
    ERROR_MYSQL_DATABASE("x253", 252),
    ERROR_SQLITE_DATABASE("x254", 253),
    ERROR_HASHING_PASSWORD("x255", 254),
    ERROR_CREATE_ACCOUNT("x256", 255),
    ERROR_ZIPPING_FILE("x257", 256);

    final String errorCode;
    final int errorId;

    ErrorCode(String errorCode, int errorId) {
        this.errorCode = errorCode;
        this.errorId = errorId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getErrorId() {
        return errorId;
    }

    /**
     * Retrieves the ErrorCode associated with the specified error ID.
     *
     * @param id the error ID to search for.
     * @return the ErrorCode corresponding to the given ID, or null if no match is found.
     */
    public static ErrorCode getErrorCodeById(int id) {
        for(ErrorCode errorCode : ErrorCode.values())
            if(errorCode.errorId == id)
                return errorCode;
        return null;
    }

    public String getError() {
        return "[ERROR] : " + name().replace("_", " ").replace("ERROR ", "") + " [Code] : " + errorCode;
    }

    public String getError(String message) {
        String finalMessage = "[ERROR] : " + name().replace("_", " ").replace("ERROR ", "") + " [Code] : " + errorCode;
        return finalMessage + " [Message] : " + message;
    }
}
