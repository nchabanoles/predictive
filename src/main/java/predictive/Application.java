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
@ConfigurationProperties("h2")
public class Application implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... strings) throws Exception {

        log.info("Building model from data...");

        log.info("Querying for archived flownode instances records:");
        FlowNodeEventProcessor processor = new FlowNodeEventProcessor();
        jdbcTemplate.query(
                "SELECT * FROM ARCH_FLOWNODE_INSTANCE", new Object[] {},
                (rs, rowNum) -> new FlowNodeEventLog(rs.getLong("reachedstatedate"), rs.getLong("id"), rs.getString("name"), rs.getLong("rootContainerId"), rs.getString("stateName"), rs.getLong("executedbysubstitute"))
        ).forEach(processor);

        log.info(String.format("Processed %d events.", processor.getNumberProcessedEvents()));

    }
}