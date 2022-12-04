package me.tuskdev.ban.controller;

import me.tuskdev.ban.PooledConnection;
import me.tuskdev.ban.enums.BanState;
import me.tuskdev.ban.model.Ban;

import java.util.Set;
import java.util.UUID;

public class BanController {

    private static final String QUERY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `ban` (`uuid` VARCHAR(36) NOT NULL, `author` VARCHAR(36) NOT NULL, `reason` VARCHAR(255) NOT NULL, `start` BIGINT NOT NULL, `end` BIGINT NOT NULL, `state` VARCHAR(255) NOT NULL, `suspended_by` VARCHAR(36) NULL)";
    private static final String QUERY_INSERT = "INSERT INTO `ban` (`uuid`, `author`, `reason`, `start`, `end`, `state`, `suspended_by`) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String QUERY_UPDATE = "UPDATE `ban` SET `state` = ?, `suspended_by` = ? WHERE `uuid` = ?";
    private static final String QUERY_SELECT_LAST = "SELECT * FROM `ban` WHERE `uuid` = ? ORDER BY (`end` - `start`) DESC LIMIT 1";
    private static final String QUERY_SELECT_ALL = "SELECT * FROM `ban` WHERE `uuid` = ?";
    private static final String QUERY_DELETE = "DELETE FROM `ban` WHERE `uuid` = ?";

    private final PooledConnection pooledConnection;

    public BanController(PooledConnection pooledConnection) {
        pooledConnection.registerAdapter(Ban.class, response -> new Ban(
                UUID.fromString(response.get("uuid")),
                UUID.fromString(response.get("author")),
                response.get("reason"), response.get("start"),
                response.get("end"),
                BanState.valueOf(response.getOrDefault("state", "ACTIVE").toUpperCase()),
                response.get("suspended_by") == null ? null : UUID.fromString(response.get("suspended_by")))
        );

        pooledConnection.statementAsync(QUERY_CREATE_TABLE);

        this.pooledConnection = pooledConnection;
    }

    public void insert(Ban ban) {
        pooledConnection.prepareStatementAsync(QUERY_INSERT, ban.getTarget().toString(), ban.getAuthor().toString(), ban.getReason(), ban.getStart(), ban.getEnd(), ban.getState().name(), ban.getSuspendedBy() == null ? null : ban.getSuspendedBy().toString());
    }

    public void update(Ban ban) {
        pooledConnection.prepareStatementAsync(QUERY_UPDATE, ban.getState().name(), ban.getSuspendedBy() == null ? null : ban.getSuspendedBy().toString(), ban.getTarget().toString());
    }

    public Ban select(UUID uuid) {
        return pooledConnection.selectAnyAsync(QUERY_SELECT_LAST, Ban.class, uuid.toString()).join();
    }

    public Set<Ban> selectAll(UUID uuid) {
        return pooledConnection.selectAsync(QUERY_SELECT_ALL, Ban.class, uuid.toString()).join();
    }

    public void delete(Ban ban) {
        pooledConnection.prepareStatementAsync(QUERY_DELETE, ban.getTarget().toString());
    }

}
