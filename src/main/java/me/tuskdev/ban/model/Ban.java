package me.tuskdev.ban.model;

import me.tuskdev.ban.enums.BanState;

import java.util.UUID;

public class Ban {

    private final UUID target, author;
    private final String reason;
    private final long start, end;
    private BanState state = BanState.ACTIVE;
    private UUID suspendedBy;

    public Ban(UUID target, UUID author, String reason, long start, long end) {
        this.target = target;
        this.author = author;
        this.reason = reason;
        this.start = start;
        this.end = end;
    }

    public Ban(UUID target, UUID author, String reason, long start, long end, BanState state, UUID suspendedBy) {
        this.target = target;
        this.author = author;
        this.reason = reason;
        this.start = start;
        this.end = end;
        this.state = state;
        this.suspendedBy = suspendedBy;
    }

    public UUID getTarget() {
        return target;
    }

    public UUID getAuthor() {
        return author;
    }

    public String getReason() {
        return reason;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public BanState getState() {
        return state;
    }

    public void setState(BanState state) {
        this.state = state;
    }

    public UUID getSuspendedBy() {
        return suspendedBy;
    }

    public void setSuspendedBy(UUID suspendedBy) {
        this.suspendedBy = suspendedBy;
    }

    public boolean isExpired() {
        if (end == -1) return false;

        return end <= System.currentTimeMillis();
    }

}
