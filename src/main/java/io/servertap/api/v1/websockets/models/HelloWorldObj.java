package io.servertap.api.v1.websockets.models;

public class HelloWorldObj {
    private String userName;
    private String message;

    public HelloWorldObj() {
    }

    public HelloWorldObj(String userName, String message) {
        super();
        this.userName = userName;
        this.message = message;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
