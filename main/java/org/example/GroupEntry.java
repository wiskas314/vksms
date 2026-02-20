package org.example;

public class GroupEntry {
    private final String group;
    private final String time;
    private final String audience;
    private long peerId;

    public GroupEntry(String group, String time, String audience, long peerId) {
        this.group = group;
        this.time = time;
        this.audience = audience;
        this.peerId = peerId;
    }

    public String getGroup(){
        return group;
    }
    public String getTime(){
        return time;
    }
    public String getAudience(){
        return audience;
    }
    public long getPeerId() { return peerId; }
    public void setPeerId(long peerId) { this.peerId = peerId; }
}
