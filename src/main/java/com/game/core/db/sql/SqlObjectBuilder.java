package com.game.core.db.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface SqlObjectBuilder<T> {

	T build(ResultSet resultSet) throws SQLException;
}
