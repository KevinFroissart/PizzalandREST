package fr.ulille.iut.pizzaland;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlite3.SQLitePlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

public class BDDFactory {
    private static Jdbi jdbi = null;
    private static String dbPath = "jdbc:sqlite:"
	+ System.getProperty("java.io.tmpdir")
	+ System.getProperty("file.separator")
	+ System.getProperty("user.name")
	+ "_";
	
    public static Jdbi getJdbi() {
        if ( jdbi == null ) {
            jdbi = Jdbi.create(dbPath + "pizza.db")
                .installPlugin(new SQLitePlugin())
                .installPlugin(new SqlObjectPlugin());
        }
        return jdbi;
    }

    public static void setJdbiForTests() {
        if ( jdbi == null ) {
            jdbi = Jdbi.create(dbPath + "pizza_test.db")
            .installPlugin(new SQLitePlugin())
            .installPlugin(new SqlObjectPlugin());
        }
    }

    public static boolean tableExist(String tableName) throws SQLException {
        DatabaseMetaData dbm = getJdbi().open().getConnection().getMetaData();
        ResultSet tables = dbm.getTables(null, null, tableName, null);
        boolean exist = tables.next();
        tables.close();
        return exist;
    }

    public static <T> T buildDao(Class<T> daoClass) {
        return getJdbi().onDemand(daoClass);
    }   
}
