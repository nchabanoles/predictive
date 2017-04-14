package predictive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import predictive.event.FlowNodeEventLog;
import predictive.event.ProcessInstanceEventLog;
import predictive.event.processor.FlowNodeEventProcessor;

@SpringBootApplication
@ConfigurationProperties("application")
public class Application implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... strings) throws Exception {

        log.info("Extracting data...");

        log.info("Querying for archived process instances records:");
        jdbcTemplate.query(
                "SELECT * FROM arch_process_instance", new Object[] {},
                (rs, rowNum) -> new FlowNodeEventLog(rs.getLong("id"))
        ).forEach(new FlowNodeEventProcessor());
    }
}