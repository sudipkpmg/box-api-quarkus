package gov.tn.dhs.quarkus.box.exception;

public class ServiceError extends RuntimeException {

    private int code;
    private String message;

    public ServiceError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"code\":\"").append(code).append("\",").append("\"message\":\"").append(message).append("\"}");
        return sb.toString();
    }

}
