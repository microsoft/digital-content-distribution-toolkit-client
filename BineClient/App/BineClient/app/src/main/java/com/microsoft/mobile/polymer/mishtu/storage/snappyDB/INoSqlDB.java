package com.microsoft.mobile.polymer.mishtu.storage.snappyDB;

import com.esotericsoftware.kryo.Kryo;

import java.io.Serializable;

/**
 * Custom NoSQL interface to the corresponding SnappyDB methods
 */
public interface INoSqlDB {

    void putString(String key, String value) throws NoSqlDBException;

    void putBoolean(String key, boolean value) throws NoSqlDBException;

    void putInt(String key, int value) throws NoSqlDBException;

    void putLong(String key, long value) throws NoSqlDBException;

    void putObject(String key, Object value) throws NoSqlDBException;

    void putObjectArray(String key, Object[] values) throws NoSqlDBException;

    void putSerializable(String key, Serializable value) throws NoSqlDBException;

    void putSerializableArray(String key, Serializable[] values) throws NoSqlDBException;

    String getString(String key) throws NoSqlDBException;

    boolean getBoolean(String key) throws NoSqlDBException;

    int getInt(String key) throws NoSqlDBException;

    long getLong(String key) throws NoSqlDBException;

    <T> T getObject(String key, Class<T> aClass) throws NoSqlDBException;

    <T> T[] getObjectArray(String key, Class<T> aClass) throws NoSqlDBException;

    <T extends Serializable> T getSerializable(String key, Class<T> aClass) throws NoSqlDBException;

    <T extends Serializable> T[] getSerializableArray(String key, Class<T> aClass) throws NoSqlDBException;

    boolean containsKey(String key) throws NoSqlDBException;

    void deleteKey(String key) throws NoSqlDBException;

    String[] findKeysByPrefix(String prefix) throws NoSqlDBException;

    String[] findKeysIteratorByPrefix(String prefix, int offset, int pageSize) throws NoSqlDBException;

    Kryo getKryoSerializer();

    void refreshDB();

    void setUpdateListener(IDBUpdateListener updateListener);

}
