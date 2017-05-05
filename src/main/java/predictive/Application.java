package predictive;

import java.util.Optional;
import java.util.Scanner;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
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

        jdbcTemplate.query(LIST_FLOWNODES_OF_COMPLETED_INSTANCES, new Object[] {},
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
                    System.out.print("Command: ");
                    command = scanner.nextLine();

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

        String processName = scanner.nextLine();
        System.out.print("Step Name: ");
        String stepName = scanner.nextLine();

        Optional<DescriptiveStatistics> predictions = processStats.getPrediction(processName, stepName, true);
        String message = String.format("No prediction available for process %s and task %s", processName, stepName);
        if(predictions.isPresent() && predictions.get().getN()>1) {
            DescriptiveStatistics stats = predictions.get();

            long confMin = new Double(stats.getMean()).longValue() - getConfidenceIntervalWidth(stats, 0.05);
            long confMax = new Double(stats.getMean()).longValue() + getConfidenceIntervalWidth(stats, 0.05);

            String lowerBond ="before ";
            if(confMin>0L) {
                lowerBond = " between " + toReadableDuration(confMin) + " and ";
            }
            String upperBond = toReadableDuration(confMax);

            message = String.format("Estimation: The case should end %s %s from now.", lowerBond, upperBond);
        }

        System.out.println(message);
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

    private long getConfidenceIntervalWidth(StatisticalSummary statistics, double significance) {
        TDistribution tDist = new TDistribution(statistics.getN() - 1);
        double a = tDist.inverseCumulativeProbability(1.0 - significance / 2);
        return new Double(a * statistics.getStandardDeviation() / Math.sqrt(statistics.getN())).longValue();
    }

    private void listStats(ProcessStats processStats) {
        processStats.printStats(System.out);
    }
}