package org.parosproxy.paros.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;


public class DbUtils {

    private static final Logger logger = Logger.getLogger(DbUtils.class);
    
    private DbUtils() {
    }
    
    /**
     * Tells whether the table {@code tableName} exists in the database, or not.
     * 
     * @param connection
     *            the connection to the database
     * @param tableName
     *            the name of the table that will be checked
     * @return {@code true} if the table {@code tableName} exists in the
     *         database, {@code false} otherwise.
     * @throws SQLException
     *             if an error occurred while checking if the table exists
     */
    public static boolean hasTable(final Connection connection, final String tableName) throws SQLException {
        boolean hasTable = false;
        
        ResultSet rs = null;
        try {
            rs = connection.getMetaData().getTables(null, null, tableName, null);
            if (rs.next()) {
                hasTable = true;
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug(e.getMessage(), e);
                }
            }
        }
        
        return hasTable;
    }
    
    /**
     * Tells whether the table {@code tableName} has the column with the given
     * {@code columnName}, or not.
     * 
     * @param connection
     *            the connection to the database
     * @param tableName
     *            the name of the table that may have the column
     * @param columnName
     *            the name of the column that will be checked
     * @return {@code true} if the table {@code tableName} has the column
     *         {@code columnName}, {@code false} otherwise.
     * @throws SQLException
     *             if an error occurred while checking if the table has the
     *             column
     */
    public static boolean hasColumn(final Connection connection, final String tableName, final String columnName) throws SQLException {
        boolean hasColumn = false;
        
        ResultSet rs = null;
        try {
            rs = connection.getMetaData().getColumns(null, null, tableName, columnName);
            if (rs.next()) {
                hasColumn = true;
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug(e.getMessage(), e);
                }
            }
        }
        
        return hasColumn;
    }

    /**
     * Gets the type of the given column {@code columnName} of the table
     * {@code tableName}.
     * 
     * @param connection
     *            the connection to the database
     * @param tableName
     *            the name of the table that has the column
     * @param columnName
     *            the name of the column that will be used to get the type
     * @return the type of the column, or -1 if the column doesn't exist.
     * @throws SQLException
     *             if an error occurred while checking the type of the column
     */
    public static int getColumnType(final Connection connection, final String tableName, final String columnName) throws SQLException {
        int columnType = -1;
        
        ResultSet rs = null;
        try {
            rs = connection.getMetaData().getColumns(null, null, tableName, columnName);
            if (rs.next()) {
                columnType = rs.getInt("SQL_DATA_TYPE");
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug(e.getMessage(), e);
                }
            }
        }
        
        return columnType;
    }
    
    /**
     * Executes and closes the given {@code preparedStatement}.
     * 
     * @param preparedStatement
     *            the statement that will be executed and immediately closed
     * @throws SQLException
     *             if error occurred while executing the given
     *             {@code preparedStatement}
     * @see PreparedStatement#close()
     * @see PreparedStatement#execute()
     */
    public static void executeAndClose(final PreparedStatement preparedStatement) throws SQLException {
        try {
            preparedStatement.execute();
        } finally {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug(e.getMessage(), e);
                }
            }
        }
    }
    
}
