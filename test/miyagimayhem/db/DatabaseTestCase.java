package miyagimayhem.db;

import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * Base class for tests that need the database. Each test gets a brand new
 * throwaway Derby database in a temporary folder, so the real database in
 * the project is never touched and tests can't affect each other.
 */
public abstract class DatabaseTestCase {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    protected DatabaseManager database;

    @Before
    public void openFreshDatabase() {
        DatabaseManager.useDatabasePath(new File(folder.getRoot(), "testDB").getAbsolutePath());
        database = DatabaseManager.getInstance();
    }

    @After
    public void closeDatabase() {
        database.shutdown();
    }
}
