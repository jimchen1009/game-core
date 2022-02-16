package com.game.core.cache.source.sql;

import com.game.core.db.sql.SqlDb;
import com.game.core.db.sql.SqlDbs;


public class SqlDbQueryUtil {

    public static SqlDb getSQLDb(){
        return SqlDbs.get("mydb");
    }
}
