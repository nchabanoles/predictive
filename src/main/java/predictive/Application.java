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
import predictive.event.processor.collectors.ProcessStats;
import predictive.event.processor.collectors.StatsCollector;

@SpringBootApplication
@ConfigurationProperties("application")
public class Application implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private static final String LIST_FLOWNODES_OF_COMPLETED_INSTANCES = "SELECT fni.ARCHIVEDATE, fni.LOGICALGROUP1, fni.NAME, fni.ROOTCONTAINERID, fni.EXECUTEDBYSUBSTITUTE, pi.STARTEDBY, pi.STARTDATE, pi.ENDDATE FROM ARCH_PROCESS_INSTANCE pi, ARCH_FLOWNODE_INSTANCE fni WHERE fni.ROOTCONTAINERID=pi.ROOTPROCESSINSTANCEID AND pi.ENDDATE>0 AND pi.ROOTPROCESSINSTANCEID=pi.SOURCEOBJECTID AND fni.TERMINAL=1 ORDER BY pi.ARCHIVEDATE ASC";

    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... strings) throws Exception {

        log.info("Building model from data...");

        final ProcessStats processStats = new ProcessStats();
        extractData(processStats);

        log.info("Model built (ready for prediction).");
        interpretCommand(processStats);
    }

    private void extractData(ProcessStats processStats) {

        jdbcTemplate.query(LIST_FLOWNODES_OF_COMPLETED_INSTANCES.toLowerCase(), new Object[] {},
                (rs, rowNum) -> new FlowNodeCompletedEvent(rs.getLong("STARTDATE"),rs.getLong("ENDDATE"),rs.getLong("ARCHIVEDATE"), rs.getLong("LOGICALGROUP1") + "-" + rs.getString("name"), rs.getLong("rootContainerId"), rs.getLong("executedbysubstitute"))
        ).forEach(fnce -> {
            StatsCollector processor = new StatsCollector(processStats, fnce.getCaseStartTime(),fnce.getCaseEndTime());
            processor.accept(fnce);
        });
    }

    private void interpretCommand(ProcessStats processStats) {

        printUsage();

            String command;
            boolean loop = true;
            try (Scanner scanner = new Scanner(System.in))
            {
                while(loop) {
                    System.out.print("\nCommand: ");
                    command = scanner.nextLine();

                    switch (command){
                        case "stats": listStats(processStats);
                            break;
                        case "predict": askForPrediction(processStats, scanner);
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
        System.out.println("** askForPrediction: allow you to ask for prediction of case duration depending a processId and StepName");
        System.out.println("** <anything else>: quit this program and loses all computed data (in-memory)");
        System.out.println("*************************************************************************************************\n");
    }

    private void listStats(ProcessStats processStats) {
        processStats.printStats(System.out);
    }

    private void askForPrediction(ProcessStats processStats, Scanner scanner) {

        System.out.println(String.format("Available processes and tasks: %s ", processStats.listAvailableStats()));

        System.out.print("\nProcess ID: ");

        String processName = scanner.nextLine();
        System.out.print("\nStep Name: ");
        String stepName = scanner.nextLine();

        String message = predict(processStats, processName, stepName);

        System.out.println(message);
    }

    private String predict(ProcessStats processStats, String processName, String stepName) {

        Optional<DescriptiveStatistics> predictions = processStats.getPrediction(processName, stepName, true);

        String message = String.format("No prediction available for process %s and task %s", processName, stepName);

        if(predictions.isPresent() && predictions.get().getN()>1) {
            DescriptiveStatistics stats = predictions.get();

            // Confidence interval: for any x in the dataset we are 95% confident that confMin < x < confMax
            // We assume that with a big dataset, data will follow a Normal Law, as assumed in data-science
            // In a Normal Law there is 95% chance that any value of the dataset will be
            // between (Avg - (1.96*standard deviation)) and (Avg +(1.96*standard deviation))
            long confMin = new Double(stats.getMean()).longValue() - (long)(1.96*stats.getStandardDeviation());
            long confMax = new Double(stats.getMean()).longValue() + (long)(1.96*stats.getStandardDeviation());

            message = buildPredictionMessage(processStats, processName, stepName, stats, confMin, confMax);
        }
        return message;
    }


    private String buildPredictionMessage(ProcessStats processStats, String processName, String stepName, DescriptiveStatistics stats, long confMin, long confMax) {

        String lowerBond ="before ";
        if(confMin>0L) {
            lowerBond = " between " + toReadableDuration(confMin) + " and ";
        }
        String upperBond = toReadableDuration(confMax);

        String message = String.format("\nEstimation: There is 95 percent chance that the case ends %s %s from now.", lowerBond, upperBond);

        // The smaller the RMSE is, the better
        long rmse = processStats.computeRMSE(processName, stepName, true);
        message += "\nPrediction: " + toReadableDuration((long)stats.getMean()) + "(+/- " + toReadableDuration(rmse) + ")";
        return message;
    }

    private String toReadableDuration(long milliseconds) {
        double calc;
        calc= milliseconds/3600000d;
        int hours = (int)calc;
        calc = calc - hours;
        calc = calc*60;
        int minutes = (int)calc;
        calc = calc - minutes;
        calc = calc*60;
        int seconds = (int)calc;
        return String.format("%s hours %s minutes %s seconds", hours, minutes, seconds);
    }
}