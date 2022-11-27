package com.microsoft.mobile.polymer.mishtu.storage.snappyDB;

import android.content.Context;

import com.esotericsoftware.kryo.Kryo;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.KeyIterator;

import com.snappydb.SnappydbException;

import java.io.Serializable;
import java.util.HashMap;

public final class SnappyDB implements INoSqlDB, INoSqlDbSettings {
    private static final String LOG_TAG = "SnappyDB";
    private static final String KILL_METHOD_APP_PREFERENCE = "AppPreference";

    public static Context sInitContext;
    public static final String SNAPPY_DB_FOLDER_NAME = "snappydb";
    //This is initialized in PolymerApplication to early intialize snappy db by loading the required so file and make the app boot faster.
    // It is a hacky way to improvise the boot time
    private final Context mContext;
    private DB mDB;
    public final Object mSnappyDBLock = new Object();
    private IDBUpdateListener mUpdateListener;

    private SnappyDB(Context context) throws SnappydbException {
        mContext = context;
        try {
            mDB = DBFactory.open(mContext);
        } catch (SnappydbException e) {
        }
    }

    private static class SnappyDBInstanceHolder {
        static final SnappyDB Instance = createInstance();

        private static SnappyDB createInstance() {
            //Retry Config
            final int MAX_ATTEMPT = 4;
            final int DELAY_IN_RETRY_IN_MS = 500;

            int currentAttempt = 0;
            SnappydbException lastSnappyError = null;
            HashMap<String, String> reportHashMap = null;
            
            while (currentAttempt < MAX_ATTEMPT) {
                try {
                    SnappyDB instance = new SnappyDB(SnappyDB.sInitContext);
                    return instance;
                } catch (SnappydbException e) {
                    lastSnappyError = e;
                    // Create the report map only when we see †he failure for the very first time.
                    currentAttempt++;
                    try {
                        Thread.sleep(DELAY_IN_RETRY_IN_MS);
                    } catch (InterruptedException e1) {
                        // no-op, we will still make retry attempt.
                    }
                }
            }
            //since we will crash the app , logging telemetry in sync to avoid loss of data.

            // This is additional telemetry/logging for crash reporting, Snappy DB failure telemetry is detailed

            // Set App Faulted and exit
            handleInitializerExceptionError(lastSnappyError);

            return null;
        }

        private static void handleInitializerExceptionError(SnappydbException exception) {
            if (exception != null) {
                /*int posixErrorCode = ((SnappydbException) exception).getPosixErrorCode();
                if (posixErrorCode == 28 *//* ENOSPC *//*) {
                    // if snappy exception is due to low memory on device.
                } else if (posixErrorCode == 30 *//* EROFS *//*) {
                    // if snappy exception is due to read only memory on device.
                } else if (posixErrorCode == 11 *//* EAGAIN *//* || posixErrorCode == 5 *//* EIO*//* ||
                        posixErrorCode == 13 *//* EACCES *//* || posixErrorCode == -1 *//* Default case *//*) {
                    // We are unable to clear lock issue, notify user that device restart might be required
                }*/
            }
        }
    }


    /**
     * Get the instance for Snappy DB.
     * CAREFUL: Do not call this method directly. Always use Store to get the SnappyDB instance.
     * The method has to be package private but due to project layering we have not done it for now.
     *
     * @param context
     * @return
     */
    public static SnappyDB getInstance(Context context) {
        sInitContext = context.getApplicationContext();

        synchronized (SnappyDB.class) {
            // We need to always get the instance in synchronized manner to avoid two thread trying to open the Snappy DB at the same time.
            return SnappyDBInstanceHolder.Instance;
        }
    }

    public void setUpdateListener(IDBUpdateListener updateListener) {
        mUpdateListener = updateListener;
    }

    public void setSyncWrite(boolean shouldSyncWrite) {
        //mDB.setSyncVal(shouldSyncWrite);
    }

    private void notifyUpdate(String key) {
        if (mUpdateListener != null) {
            mUpdateListener.onUpdate(key);
        }
    }

    @Override
    public boolean containsKey(String key) throws NoSqlDBException {
        try {
            synchronized (mSnappyDBLock) {
                return mDB.exists(key);
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new NoSqlDBException(e);

        }
    }

    @Override
    public void putString(String key, String value) throws NoSqlDBException {
        try {
            synchronized (mSnappyDBLock) {
                mDB.put(key, value);
            }
            notifyUpdate(key);
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new NoSqlDBException(e);
        }
    }

    @Override
    public void putInt(String key, int value) throws NoSqlDBException {
        try {
            synchronized (mSnappyDBLock) {
                mDB.putInt(key, value);
            }
            notifyUpdate(key);
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new NoSqlDBException(e);
        }
    }

    @Override
    public void putLong(String key, long value) throws NoSqlDBException {
        try {
            synchronized (mSnappyDBLock) {
                mDB.putLong(key, value);
            }
            notifyUpdate(key);
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new NoSqlDBException(e);
        }
    }

    @Override
    public void putBoolean(String key, boolean value) throws NoSqlDBException {
        try {
            synchronized (mSnappyDBLock) {
                mDB.putBoolean(key, value);
            }
            notifyUpdate(key);
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new NoSqlDBException(e);
        }
    }

    @Override
    public void putObject(String key, Object value) throws NoSqlDBException {
        try {
            synchronized (mSnappyDBLock) {
                mDB.put(key, value);
            }
            notifyUpdate(key);
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new NoSqlDBException(e);
        }
    }

    @Override
    public void putObjectArray(String key, Object[] values) throws NoSqlDBException {
        try {
            synchronized (mSnappyDBLock) {
                mDB.put(key, values);
            }
            notifyUpdate(key);
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new NoSqlDBException(e);
        }
    }

    @Override
    public void putSerializable(String key, Serializable value) throws NoSqlDBException {
        try {
            synchronized (mSnappyDBLock) {
                mDB.put(key, value);
            }
            notifyUpdate(key);
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new NoSqlDBException(e);
        }
    }

    @Override
    public void putSerializableArray(String key, Serializable[] values) throws NoSqlDBException {
        try {
            synchronized (mSnappyDBLock) {
                mDB.put(key, values);
            }
            notifyUpdate(key);
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new NoSqlDBException(e);
        }
    }

    @Override
    public void deleteKey(String key) throws NoSqlDBException {
        try {
            synchronized (mSnappyDBLock) {
                mDB.del(key);
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new NoSqlDBException(e);
        }
    }

    @Override
    public String getString(String key) throws NoSqlDBException {
        try {
            synchronized (mSnappyDBLock) {
                return mDB.get(key);
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new NoSqlDBException(e);
        }
    }

    @Override
    public boolean getBoolean(String key) throws NoSqlDBException {
        try {
            synchronized (mSnappyDBLock) {
                return mDB.getBoolean(key);
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new NoSqlDBException(e);
        }
    }

    @Override
    public int getInt(String key) throws NoSqlDBException {
        synchronized (mSnappyDBLock) {
            try {
                return mDB.getInt(key);
            } catch (SnappydbException e) {
                e.printStackTrace();
                throw new NoSqlDBException(e);
            }
        }
    }

    @Override
    public long getLong(String key) throws NoSqlDBException {
        synchronized (mSnappyDBLock) {
            try {
                return mDB.getLong(key);
            } catch (SnappydbException e) {
                e.printStackTrace();
                throw new NoSqlDBException(e);
            }
        }
    }

    @Override
    public <T extends Serializable> T getSerializable(String key, Class<T> aClass) throws NoSqlDBException {
        try {
            synchronized (mSnappyDBLock) {
                return mDB.get(key, aClass);
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new NoSqlDBException(e);
        }
    }

    @Override
    public <T extends Serializable> T[] getSerializableArray(String key, Class<T> aClass) throws NoSqlDBException {
        try {
            synchronized (mSnappyDBLock) {
                return mDB.getArray(key, aClass);
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new NoSqlDBException(e);
        }
    }

    @Override
    public <T> T getObject(String key, Class<T> aClass) throws NoSqlDBException {
        try {
            synchronized (mSnappyDBLock) {
                return mDB.getObject(key, aClass);
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new NoSqlDBException(e);
        }
    }

    @Override
    public <T> T[] getObjectArray(String key, Class<T> aClass) throws NoSqlDBException {
        try {
            synchronized (mSnappyDBLock) {
                return mDB.getObjectArray(key, aClass);
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new NoSqlDBException(e);
        }
    }

    @Override
    public String[] findKeysByPrefix(String prefix) throws NoSqlDBException {
        try {
            synchronized (mSnappyDBLock) {
                return mDB.findKeys(prefix);
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new NoSqlDBException(e);
        }
    }

    @Override
    public String[] findKeysIteratorByPrefix(String prefix, int offset, int pageSize) throws NoSqlDBException {
        try {
            synchronized (mSnappyDBLock) {
                return mDB.findKeys(prefix, offset, pageSize);
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new NoSqlDBException(e);
        }
    }

    public KeyIterator allKeysIterator() throws NoSqlDBException {
        try {
            synchronized (mSnappyDBLock) {
                return mDB.allKeysIterator();
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new NoSqlDBException(e);
        }
    }
    @Override
    public Kryo getKryoSerializer() {
        return mDB.getKryoInstance();
    }

    @Override
    public void refreshDB() {
        try {
            mDB.destroy();
            mDB = DBFactory.open(mContext);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public void closeDB() {
        try {
            mDB.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public void openDB() {
        try {
            mDB = DBFactory.open(mContext);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }
}
