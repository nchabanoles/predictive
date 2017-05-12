package predictive.configuration;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.postgresql.jdbc2.optional.PoolingDataSource;
import org.postgresql.jdbc2.optional.SimpleDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import oracle.jdbc.pool.OracleDataSource;

@Configuration
@ConfigurationProperties("postgres")
@Conditional(PostgresDatasourceNeededCondition.class)
public class PostgresConfiguration {

    private String username;

    private String password;

    private String url;
    private String serverName;

    private String databaseName;

    private int portNumber;
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    @Bean
    DataSource dataSource() throws SQLException {

        SimpleDataSource dataSource = new SimpleDataSource();

        dataSource.setUser(username);
        dataSource.setPassword(password);
        dataSource.setServerName(serverName);
        dataSource.setDatabaseName(databaseName);
        dataSource.setPortNumber(portNumber);

        return dataSource;
    }
}