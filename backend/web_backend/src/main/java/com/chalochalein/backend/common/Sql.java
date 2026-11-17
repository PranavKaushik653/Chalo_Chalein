package com.chalochalein.backend.common;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public final class Sql {
    private Sql(){}

    public static List<String> stringList(ResultSet rs, String col) throws SQLException {
        Array a = rs.getArray(col);
        return a == null ? List.of() : Arrays.asList((String[]) a.getArray());
    }
}
