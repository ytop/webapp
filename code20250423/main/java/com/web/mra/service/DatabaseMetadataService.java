package com.web.mra.service;

import com.web.mra.dto.TableMetadataDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseMetadataService {

    private final JdbcTemplate jdbcTemplate;

    public List<String> getAllTables() {
        String sql;
        // log.info("Retrieving all tables from DatabaseMetadataService");
        sql = " SELECT t.TABLE_NAME as TableName FROM INFORMATION_SCHEMA.TABLES t " +
                "WHERE  t.TABLE_TYPE = 'BASE TABLE' and  t.TABLE_SCHEMA = 'web' " +
                "AND (t.TABLE_NAME =  'IMP_DETECTION_DICT' OR t.TABLE_NAME = 'IMP_DETECTION_EXCEL_META')" + 
                "ORDER BY  t.TABLE_NAME ";

        return jdbcTemplate.queryForList(sql, String.class);
    }

    public List<TableMetadataDTO> getTableMetadata(String tableName) {
        String sql;
        sql = " SELECT \n" +
                "    c.COLUMN_NAME,\n" +
                "    c.DATA_TYPE,\n" +
                "    c.IS_NULLABLE,\n" +
                "    CASE \n" +
                "        WHEN pk.COLUMN_NAME IS NOT NULL THEN 'PK'\n" +
                "        ELSE NULL \n" +
                "    END AS COLUMN_KEY,\n" +
                "   c.CHARACTER_MAXIMUM_LENGTH\n" +
                "FROM \n" +
                "    INFORMATION_SCHEMA.COLUMNS c\n" +
                "    LEFT JOIN (\n" +
                "        SELECT \n" +
                "            ku.TABLE_CATALOG,\n" +
                "            ku.TABLE_SCHEMA,\n" +
                "            ku.TABLE_NAME,\n" +
                "            ku.COLUMN_NAME\n" +
                "        FROM \n" +
                "            INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc\n" +
                "            JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE ku\n" +
                "                ON tc.CONSTRAINT_NAME = ku.CONSTRAINT_NAME\n" +
                "        WHERE \n" +
                "            tc.CONSTRAINT_TYPE = 'PRIMARY KEY'\n" +
                "    ) pk ON c.TABLE_CATALOG = pk.TABLE_CATALOG \n" +
                "        AND c.TABLE_SCHEMA = pk.TABLE_SCHEMA \n" +
                "        AND c.TABLE_NAME = pk.TABLE_NAME \n" +
                "        AND c.COLUMN_NAME = pk.COLUMN_NAME \n" +
                "WHERE \n" +
                "    c.TABLE_NAME = ? \n" +
                "    AND c.TABLE_SCHEMA = 'web'\n" +
                "ORDER BY \n" +
                "    c.ORDINAL_POSITION;\n" +
                "      \n ";


        return jdbcTemplate.query(sql,
                (rs, rowNum) -> TableMetadataDTO.builder()
                        .columnName(rs.getString("COLUMN_NAME"))
                        .dataType(mapDataType(rs.getString("DATA_TYPE")))
                        .nullable("YES".equals(rs.getString("IS_NULLABLE")))
                        .isPrimaryKey("PRI".equals(rs.getString("COLUMN_KEY")))
                        .maxLength(rs.getObject("CHARACTER_MAXIMUM_LENGTH", Integer.class))
                        .build(),
                tableName
        );
    }

    private String mapDataType(String sqlType) {
        switch (sqlType.toLowerCase()) {
            case "datetime":
            case "timestamp":
                return "datetime";
            case "int":
            case "bigint":
            case "decimal":
            case "numeric":
                return "number";
            case "text":
            case "mediumtext":
            case "longtext":
                return "text";
            case "bit":
            case "boolean":
                return "boolean";
            default:
                return "text";
        }
    }
}
