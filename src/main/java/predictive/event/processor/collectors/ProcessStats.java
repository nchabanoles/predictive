package predictive.event.processor.collectors;

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import predictive.data.PercentileSampler;
import predictive.event.FlowNodeCompletedEvent;

public class ProcessStats {

    private LinkedHashMap<String, List<Long>> elapseTimes = new LinkedHashMap<>();
    private LinkedHashMap<String, List<Long>> sejournTimes = new LinkedHashMap<>();
    private LinkedHashMap<String, List<Long>> remainingTimes = new LinkedHashMap<>();

    public ProcessStats() {
    }

    public void printStats(PrintStream out) {
        sejournTimes.entrySet().forEach(entry -> {
            out.println(String.format("\t\t\tSejourn times for state %s :\n%s", entry.getKey(), entry.getValue()));
        });
        remainingTimes.entrySet().forEach(entry -> {
            out.println(String.format("\t\t\tRemaining times for state %s :\n%s", entry.getKey(), entry.getValue()));
        });
        elapseTimes.entrySet().forEach(entry -> {
            out.println(String.format("\t\t\tElapse times for state %s :\n%s", entry.getKey(), entry.getValue()));
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

    private List<Long> storeTime(LinkedHashMap<String, List<Long>> map, FlowNodeCompletedEvent event, Long time) {

        // 1st list is the full list of observations
        List<Long> vector = map.getOrDefault(event.getEventKey(), new ArrayList());
        vector.add(time);

        // Make sure the vector is persisted
        map.putIfAbsent(event.getEventKey(), vector);

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

        String eventKey = processID+"-"+stepName;
        if(remainingTimes.containsKey(eventKey)) {
                DescriptiveStatistics stats = new DescriptiveStatistics();
            remainingTimes.get(eventKey).forEach(stats::addValue);
                if(percentile90Only) {
                    // Only keep the 90 Percentile to improve robustness
                    double p90 = stats.getPercentile(90);
                    stats.clear();
                    remainingTimes.get(eventKey).stream().filter(l -> l<=p90).forEach(stats::addValue);
                }
                return Optional.of(stats);
        }
        return Optional.empty();

    }

    public Map<String, List<String>> listAvailableStats() {
        final Map<String, List<String>> result = new HashMap<>();
        remainingTimes.keySet().forEach(k ->
        {
            String[] eventKey = k.split("-");
            String processName = eventKey[0];
            String stepName = eventKey[1];
            List<String> processSteps = result.getOrDefault(processName, new ArrayList<>());
            processSteps.add(stepName);
            result.put(processName, processSteps);
        });
        return result;
    }

    public long computeRMSE(String processID, String stepName, boolean percentile90Only) {
            // filter 90 percentile
            // take out 20% (randomly)
            // check avg(80%) == avg(20%)
            // add to result sqrt(sigma20((avg(80) - item(20))²)/n(20))

        List<Long> items = remainingTimes.get(processID+"-"+stepName);
        long maxValue = Long.MAX_VALUE;
        if(percentile90Only) {
            maxValue = compute90Percentile(items);
        }

        PercentileSampler sampler = new PercentileSampler((int) (items.size() * 0.8), maxValue);

        List<Long> sample = items.stream()
                .collect(sampler);
        logSet("Training set", sample);

        final long mean = (long)sample.stream().mapToLong(l->l).average().getAsDouble();

        List<Long> testSet = sampler.listOmittedValues();
        logSet("Test set", testSet);

        Long sum = testSet.stream().reduce(0l, (acc, i) -> acc + (long)(Math.pow(mean - i, 2)));

        // The smaller the RMSE is the better
        // Olan gets 3500
        return (long)Math.sqrt(sum / testSet.size());




    }

    private void logSet(String label, List<Long> set) {
        System.out.println("************");
        System.out.println(label +":");
        set.stream().forEach(System.out::println);
        System.out.println("************");
    }

    private long compute90Percentile(List<Long> items) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        items.stream().forEach(stats::addValue);
        return (long)stats.getPercentile(90);
    }

}