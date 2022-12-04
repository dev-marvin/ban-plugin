package me.tuskdev.ban;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.tuskdev.ban.database.QueryAdapter;
import me.tuskdev.ban.database.QueryResponse;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public class PooledConnection {

    private final Map<Class<?>, QueryAdapter<?>> ADAPTER_MAP = new HashMap<>();
    private final ExecutorService EXECUTOR = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    private final HikariDataSource hikariDataSource;

    PooledConnection(String folder, String file) {
        this.hikariDataSource = new HikariDataSource(new HikariConfig(folder + File.separator + file));
    }

    public <T> void registerAdapter(Class<T> clazz, QueryAdapter<T> adapter) {
        ADAPTER_MAP.put(clazz, adapter);
    }

    public void statement(String query) {
        try (Connection connection = hikariDataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void statementAsync(String query) {
        EXECUTOR.submit(() -> statement(query));
    }

    public void prepareStatement(String query, Object... parameters) {
        try (Connection connection = hikariDataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (int i = 0; i < parameters.length; i++) preparedStatement.setObject(i + 1, parameters[i]);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void prepareStatementAsync(String query, Object... parameters) {
        EXECUTOR.submit(() -> prepareStatement(query, parameters));
    }

    public void prepareStatement(String query, List<Object[]> list) {
        try (Connection connection = hikariDataSource.getConnection()) {
            for (Object[] parameters : list) {
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                for (int i = 0; i < parameters.length; i++) preparedStatement.setObject(i + 1, parameters[i]);

                preparedStatement.execute();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void prepareStatementAsync(String query, List<Object[]> list) {
        EXECUTOR.submit(() -> prepareStatement(query, list));
    }

    public <T> T selectAny(String query, Class<T> classAdapter, Object... parameters) {
        try (Connection connection = hikariDataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (int i = 0; i < parameters.length; i++) preparedStatement.setObject(i + 1, parameters[i]);

            ResultSet resultSet = preparedStatement.executeQuery();

            QueryAdapter<T> adapter = (QueryAdapter<T>) ADAPTER_MAP.get(classAdapter);
            return adapter != null && resultSet.next() ? adapter.accept(QueryResponse.of(resultSet)) : null;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
}

    public <T> CompletableFuture<T> selectAnyAsync(String query, Class<T> classAdapter, Object... parameters) {
        return CompletableFuture.supplyAsync(() -> selectAny(query, classAdapter, parameters), EXECUTOR);
    }

    public <T> Set<T> select(String query, Class<T> classAdapter, Object... parameters) {
        try (Connection connection = hikariDataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (int i = 0; i < parameters.length; i++) preparedStatement.setObject(i + 1, parameters[i]);

            ResultSet resultSet = preparedStatement.executeQuery();

            Set<T> set = new HashSet<>();
            QueryAdapter<T> queryAdapter = (QueryAdapter<T>) ADAPTER_MAP.get(classAdapter);
            while (resultSet.next() && queryAdapter != null) set.add(queryAdapter.accept(QueryResponse.of(resultSet)));

            return set;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Collections.emptySet();
    }

    public <T> CompletableFuture<Set<T>> selectAsync(String query, Class<T> classAdapter, Object... parameters) {
        return CompletableFuture.supplyAsync(() -> select(query, classAdapter, parameters), EXECUTOR);
    }

    void close() {
        EXECUTOR.shutdown();
        hikariDataSource.close();
    }

}
