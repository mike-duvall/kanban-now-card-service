package kanbannow.health;

import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.metrics.core.HealthCheck;
import org.skife.jdbi.v2.DBI;

public class DatabaseHealthCheck extends HealthCheck {


    private DatabaseConfiguration databaseConfiguration;

    public DatabaseHealthCheck(DatabaseConfiguration aDatabaseConfiguration) {
        super("cardService");
        this.databaseConfiguration = aDatabaseConfiguration;
    }

    @Override
    protected Result check() throws Exception {
        String databaseDriverClassName = databaseConfiguration.getDriverClass();
        Class.forName(databaseDriverClassName);
        String dataSourceUrl = databaseConfiguration.getUrl();
        String dataSourceUsername = databaseConfiguration.getUser();
        String dataSourcePassword = databaseConfiguration.getPassword();
        new DBI(dataSourceUrl, dataSourceUsername, dataSourcePassword );
        return Result.healthy();
    }
}
