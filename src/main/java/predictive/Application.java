package predictive;

import java.io.Console;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import predictive.event.FlowNodeCompletedEvent;
import predictive.event.ProcessInstanceEventLog;
import predictive.event.processor.FlowNodeEventProcessor;
import predictive.event.processor.collectors.ProcessStats;

@SpringBootApplication
@ConfigurationProperties("application")
public class Application implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    public static final String LIST_COMPLETED_ROOT_PROCESS_INSTANCES = "SELECT * FROM ARCH_PROCESS_INSTANCE WHERE ENDDATE > 0 AND ROOTPROCESSINSTANCEID=SOURCEOBJECTID ORDER BY ARCHIVEDATE ASC"; //AND ROWNUM <= 1
    /*
    * Force ordering on ARCHIVEDATE.
    * !! Attention !! in case of // or multi-instance we have completedCases of steps that are mixed !!!
    * */
    public static final String LIST_COMPLETED_FLOW_NODES_OF_PROCESS_INSTANCE = "SELECT * FROM ARCH_FLOWNODE_INSTANCE WHERE ROOTCONTAINERID=? AND TERMINAL=1 ORDER BY ARCHIVEDATE ASC";

    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }

    static long completedCases = 0L;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... strings) throws Exception {

        log.info("Building model from data...");

        final ProcessStats processStats = new ProcessStats();

        //List cases completed
        jdbcTemplate.query(
                LIST_COMPLETED_ROOT_PROCESS_INSTANCES, new Object[] {},
                (rs, rowNum) -> new ProcessInstanceEventLog(rs.getLong("ARCHIVEDATE"), rs.getLong("ROOTPROCESSINSTANCEID"), rs.getLong("PROCESSDEFINITIONID"), rs.getLong("STARTEDBY"),rs.getLong("STARTDATE"),rs.getLong("ENDDATE"))
        ).forEach(piel -> {
            Application.completedCases++;
            FlowNodeEventProcessor processor = new FlowNodeEventProcessor(processStats, piel.getStartTime(), piel.getCompletionTime());

            jdbcTemplate.query(
                    LIST_COMPLETED_FLOW_NODES_OF_PROCESS_INSTANCE, new Object[] {piel.getId()},
                    (rs, rowNum) -> new FlowNodeCompletedEvent(rs.getLong("ARCHIVEDATE"), rs.getLong("LOGICALGROUP1") + "-" + rs.getString("name"), rs.getLong("rootContainerId"), rs.getLong("executedbysubstitute"))
            ).forEach(processor);

            log.debug(String.format("Process Instance %d had %d flownodes.", piel.getId(), processor.getNumberProcessedEvents()));
        });

        log.info(String.format("Processed %d cases.", Application.completedCases));

        processStats.printStats();

        askForPrediction(processStats);



    }

    private void askForPrediction(ProcessStats processStats) {

        log.info("Ready for prediction :-)");

        log.info(String.format("Available processes and tasks: %s ", processStats.listAvailableStats()));

            String processName = null;
            String stepName = null;
            try (Scanner scanner = new Scanner(System.in))
            {
                while(true) {
                    System.out.print("Process ID: ");

                    // get their input as a String
                    processName = scanner.next();
                    System.out.print("Step Name: ");
                    stepName = scanner.next();
                    DescriptiveStatistics predictions = processStats.getPrediction(processName, stepName);
                    log.info(String.format("It is more likely that your case will complete in %s milliseconds (min: %s, max: %s)", predictions.getMean(), predictions.getMin(), predictions.getMax()));
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }

    }
}