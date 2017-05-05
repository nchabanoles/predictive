package predictive.event.processor.collectors;

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import predictive.event.FlowNodeCompletedEvent;

public class ProcessStats {

    private Map<String, LinkedHashMap<String, List<Long>>> elapseTimes = new HashMap<>();
    private Map<String, LinkedHashMap<String, List<Long>>> sejournTimes = new HashMap<>();
    private Map<String, LinkedHashMap<String, List<Long>>> remainingTimes = new HashMap<>();

    public ProcessStats() {
    }

    public void printStats(PrintStream out) {
        sejournTimes.entrySet().forEach(entry -> {
            out.println(String.format("\t\t\tSejourn times for process %s :\n%s", entry.getKey(), entry.getValue()));
        });
        remainingTimes.entrySet().forEach(entry -> {
            out.println(String.format("\t\t\tRemaining times for process %s :\n%s", entry.getKey(), entry.getValue()));
        });
        elapseTimes.entrySet().forEach(entry -> {
            out.println(String.format("\t\t\tElapse times for process %s :\n%s", entry.getKey(), entry.getValue()));
        });
    }

    public List<Long> storeRemainingTimeForEvent(FlowNodeCompletedEvent event, Long remainingTime) {
        return storeTime(remainingTimes, event, remainingTime);
    }

    public List<Long> storeSejournTimeForEvent(FlowNodeCompletedEvent event, Long sejournTime) {
        return storeTime(sejournTimes, event, sejournTime);
    }

    public List<Long> storeElapseTimeForEvent(FlowNodeCompletedEvent event, long elapseTime) {
        return storeTime(elapseTimes, event, elapseTime);
    }

    private List<Long> storeTime(Map<String, LinkedHashMap<String, List<Long>>> map, FlowNodeCompletedEvent event, Long time) {

        String[] keys = event.getEventKey().split("-");
        String processName = keys[0];
        String stepName = keys[1];

        LinkedHashMap<String, List<Long>> stepsTimes = map.getOrDefault(processName, new LinkedHashMap<>());
        List<Long> vector = stepsTimes.getOrDefault(stepName, new ArrayList<>());

        vector.add(time);

        // Make sure the vector is persisted
        stepsTimes.putIfAbsent(stepName, vector);
        map.putIfAbsent(processName, stepsTimes);

        return Collections.unmodifiableList(vector);
    }

    /**
     * Get prediction of the remaining time of a case based on the step that last completed.
     * @param processID The identifier of the process definition upon which we want to compute a prediction based on its statistics
     * @param stepName The name of the last step executed in the process.
     * @param percentile90Only When set to true, the 10% highest values are ignored as they are considered potentially to far from average value.
     *                         Such values would badly influence statistics.
     * @return statistics on all observed durations between the completion of the given step and the completion of the process instance (aka remaining time).
     */
    public Optional<DescriptiveStatistics> getPrediction(String processID, String stepName, boolean percentile90Only) {

        if(remainingTimes.containsKey(processID)) {
            LinkedHashMap<String, List<Long>> steps = remainingTimes.get(processID);
            if(steps.containsKey(stepName)) {
                DescriptiveStatistics stats = new DescriptiveStatistics();
                steps.get(stepName).forEach(stats::addValue);
                if(percentile90Only) {
                    // Only keep the 90 Percentile to improve robustness
                    double p90 = stats.getPercentile(90);
                    stats.clear();
                    steps.get(stepName).stream().filter(l -> l<=p90).forEach(stats::addValue);
                }
                return Optional.of(stats);
            }

        }
        return Optional.empty();

    }

    public Map<String, List<String>> listAvailableStats() {
        final Map<String, List<String>> result = new HashMap<>();
        remainingTimes.entrySet().forEach(e -> {
            result.put(e.getKey(), e.getValue().keySet().stream().collect(Collectors.toList()));
        });
        return result;
    }
}