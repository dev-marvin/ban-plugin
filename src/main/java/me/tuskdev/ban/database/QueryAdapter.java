package me.tuskdev.ban.database;

public interface QueryAdapter<T> {

    T accept(QueryResponse queryResponse);

}
