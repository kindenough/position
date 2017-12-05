package com.example.usedaoforgen.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.example.usedaoforgen.bean.POSITION;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table POSITION.
*/
public class POSITIONDao extends AbstractDao<POSITION, Long> {

    public static final String TABLENAME = "POSITION";

    /**
     * Properties of entity POSITION.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property TEL = new Property(1, String.class, "TEL", false, "TEL");
        public final static Property X = new Property(2, Double.class, "X", false, "X");
        public final static Property Y = new Property(3, Double.class, "Y", false, "Y");
        public final static Property SPEED = new Property(4, Float.class, "SPEED", false, "SPEED");
        public final static Property ACCURACY = new Property(5, String.class, "ACCURACY", false, "ACCURACY");
        public final static Property STRTIME = new Property(6, String.class, "STRTIME", false, "STRTIME");
        public final static Property TIME = new Property(7, java.util.Date.class, "TIME", false, "TIME");
        public final static Property DEVICEID = new Property(8, String.class, "DEVICEID", false, "DEVICEID");
    };


    public POSITIONDao(DaoConfig config) {
        super(config);
    }
    
    public POSITIONDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'POSITION' (" + //
                "'_id' INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "'TEL' TEXT NOT NULL ," + // 1: TEL
                "'X' REAL," + // 2: X
                "'Y' REAL," + // 3: Y
                "'SPEED' REAL," + // 4: SPEED
                "'ACCURACY' TEXT," + // 5: ACCURACY
                "'STRTIME' TEXT," + // 6: STRTIME
                "'TIME' INTEGER," + // 7: TIME
                "'DEVICEID' TEXT);"); // 8: DEVICEID
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'POSITION'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, POSITION entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getTEL());
 
        Double X = entity.getX();
        if (X != null) {
            stmt.bindDouble(3, X);
        }
 
        Double Y = entity.getY();
        if (Y != null) {
            stmt.bindDouble(4, Y);
        }
 
        Float SPEED = entity.getSPEED();
        if (SPEED != null) {
            stmt.bindDouble(5, SPEED);
        }
 
        String ACCURACY = entity.getACCURACY();
        if (ACCURACY != null) {
            stmt.bindString(6, ACCURACY);
        }
 
        String STRTIME = entity.getSTRTIME();
        if (STRTIME != null) {
            stmt.bindString(7, STRTIME);
        }
 
        java.util.Date TIME = entity.getTIME();
        if (TIME != null) {
            stmt.bindLong(8, TIME.getTime());
        }
 
        String DEVICEID = entity.getDEVICEID();
        if (DEVICEID != null) {
            stmt.bindString(9, DEVICEID);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public POSITION readEntity(Cursor cursor, int offset) {
        POSITION entity = new POSITION( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // TEL
            cursor.isNull(offset + 2) ? null : cursor.getDouble(offset + 2), // X
            cursor.isNull(offset + 3) ? null : cursor.getDouble(offset + 3), // Y
            cursor.isNull(offset + 4) ? null : cursor.getFloat(offset + 4), // SPEED
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // ACCURACY
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // STRTIME
            cursor.isNull(offset + 7) ? null : new java.util.Date(cursor.getLong(offset + 7)), // TIME
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8) // DEVICEID
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, POSITION entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setTEL(cursor.getString(offset + 1));
        entity.setX(cursor.isNull(offset + 2) ? null : cursor.getDouble(offset + 2));
        entity.setY(cursor.isNull(offset + 3) ? null : cursor.getDouble(offset + 3));
        entity.setSPEED(cursor.isNull(offset + 4) ? null : cursor.getFloat(offset + 4));
        entity.setACCURACY(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setSTRTIME(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setTIME(cursor.isNull(offset + 7) ? null : new java.util.Date(cursor.getLong(offset + 7)));
        entity.setDEVICEID(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(POSITION entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(POSITION entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
