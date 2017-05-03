package predictive.event.processor.collectors;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import predictive.event.FlowNodeCompletedEvent;

public class ProcessStats {

    Map<String, LinkedHashMap<String, List<Long>>> elapseTimes = new HashMap<>();
    Map<String, LinkedHashMap<String, List<Long>>> sejournTimes = new HashMap<>();
    Map<String, LinkedHashMap<String, List<Long>>> remainingTimes = new HashMap<>();

    public ProcessStats() {
    }

    public void printStats() {
        sejournTimes.entrySet().forEach(entry -> {
            System.out.println(String.format("\t\t\tSejourn times for process %s :\n%s", entry.getKey(), entry.getValue()));
        });
        remainingTimes.entrySet().forEach(entry -> {
            System.out.println(String.format("\t\t\tRemaining times for process %s :\n%s", entry.getKey(), entry.getValue()));
        });
        elapseTimes.entrySet().forEach(entry -> {
            System.out.println(String.format("\t\t\tElapse times for process %s :\n%s", entry.getKey(), entry.getValue()));
        });
    }

    public List<Long> storeRemainingTimeForEvent(FlowNodeCompletedEvent event, Long remainingTime) {
        return storeTime(remainingTimes, event, remainingTime);
    }

    public List<Long> storeSejournTimeForEvent(FlowNodeCompletedEvent event, Long sejournTime) {
        // Add sejourn time to vector of computed values (ProcessDefinition-StepName scope)
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


    public Optional<DescriptiveStatistics> getPrediction(String processName, String stepName) {

        if(remainingTimes.containsKey(processName)) {
            LinkedHashMap<String, List<Long>> steps = remainingTimes.get(processName);
            if(steps.containsValue(stepName)) {
                DescriptiveStatistics stats = new DescriptiveStatistics();
                steps.get(stepName).forEach(stats::addValue);
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