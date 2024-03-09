package io.servertap.api.v1.websockets.models;

public class EconomyPayload {
    private String uuid;
    private String amount;

    public EconomyPayload(String uuid, String amount) {
        this.uuid = uuid;
        this.amount = amount;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
