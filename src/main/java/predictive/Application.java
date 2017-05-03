package predictive;

import java.util.Optional;
import java.util.Scanner;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

        interpretCommand(processStats);

    }

    private void interpretCommand(ProcessStats processStats) {

        printUsage();

            String command;
            boolean loop = true;
            try (Scanner scanner = new Scanner(System.in))
            {
                while(loop) {
                    System.out.print("Command: ");
                    command = scanner.next();

                    switch (command){
                        case "stats": listStats(processStats);
                            break;
                        case "predict": predict(processStats, scanner);
                            break;
                        case "quit": case "q":
                            loop = false;
                            break;
                        default:
                            System.out.println("Unknown command: " + command);
                            printUsage();
                            break;
                    }
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }

    }

    private void printUsage() {
        System.out.println("**************");
        System.out.println("*** Usage: ***");
        System.out.println("**************");
        System.out.println("** stats: provide detailed stats of available data");
        System.out.println("** predict: allow you to ask for prediction of case duration depending a processId and StepName");
        System.out.println("** <anything else>: quit this program and loses all computed data (in-memory)");
        System.out.println("*************************************************************************************************\n");
    }

    private void predict(ProcessStats processStats, Scanner scanner) {

        System.out.println(String.format("Available processes and tasks: %s ", processStats.listAvailableStats()));

        System.out.print("Process ID: ");

        String processName = scanner.next();
        System.out.print("Step Name: ");
        String stepName = scanner.next();
        Optional<DescriptiveStatistics> predictions = processStats.getPrediction(processName, stepName);
        String message = String.format("No prediction available for process %s and task %s", processName, stepName);
        if(predictions.isPresent()) {
            DescriptiveStatistics stats = predictions.get();
            message = String.format("It is more likely that your case will complete in %s milliseconds (min: %s, max: %s)", stats.getMean(), stats.getMin(), stats.getMax());
        }
        System.out.println(message);
    }

    private void listStats(ProcessStats processStats) {
        System.out.println(String.format("Processed %d cases.", Application.completedCases));

        processStats.printStats();
    }
}