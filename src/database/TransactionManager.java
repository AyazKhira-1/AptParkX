package database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * TransactionManager provides a centralized way to manage database transactions.
 * It handles starting, committing, and rolling back transactions.
 */
public final class TransactionManager {

    /**
     * Private constructor to prevent this utility class from being instantiated.
     */
    private TransactionManager() {
    }

    /**
     * Begins a new transaction by setting auto-commit to false.
     *
     * @param connection The connection on which to start the transaction.
     * @throws SQLException if a database access error occurs.
     */
    public static void beginTransaction(Connection connection) throws SQLException {
        if (connection != null) {
            connection.setAutoCommit(false);
        }
    }

    /**
     * Commits the current transaction.
     *
     * @param connection The connection on which to commit the transaction.
     * @throws SQLException if a database access error occurs.
     */
    public static void commitTransaction(Connection connection) throws SQLException {
        if (connection != null) {
            connection.commit();
        }
    }

    /**
     * Rolls back the current transaction in case of an error.
     *
     * @param connection The connection on which to roll back the transaction.
     */
    public static void rollbackTransaction(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                System.err.println("Failed to rollback transaction: " + e.getMessage());
            }
        }
    }

    /**
     * Resets the connection to its default auto-commit state.
     *
     * @param connection The connection to reset.
     */
    public static void endTransaction(Connection connection) {
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Failed to reset auto-commit: " + e.getMessage());
            }
        }
    }
}
