package io.servertap.mojang.api.models;

public class PlayerInfo {
    private String id;
    private String name;

    public PlayerInfo(String id, String name) {
        setId(id);
        setName(name);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        //Whitelist file doesn't accept UUIDs without dashes
        this.id = id.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
