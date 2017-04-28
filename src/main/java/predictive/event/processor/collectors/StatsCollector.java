package predictive.event.processor.collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import predictive.Application;
import predictive.event.FlowNodeCompletedEvent;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by Nicolas Chabanoles on 17/04/2017.
 */
public class StatsCollector implements Consumer<FlowNodeCompletedEvent> {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private final ProcessStats processStats;

    private long nbEventsProcessed = 0L;

    private Map<Long, FlowNodeCompletedEvent> processInstanceEvents = new HashMap<>();

    private long startDate;
    private long completionDate;

    public StatsCollector(ProcessStats processStats, long startDate, long completionDate){
        this.processStats = processStats;
        this.startDate = startDate;
        this.completionDate = completionDate;
    }


    @Override
    public void accept(FlowNodeCompletedEvent event) {
        if(event==null) {
            return; // ignore null event
        }
        final Long sejournTime = computeSejournTime(processInstanceEvents.get(event.getRootProcessInstanceId()), event);
        final Long remainingTime = computeRemainingTime(event);
        final long elapseTime = computeElapseTime(event);

        processStats.storeSejournTimeForEvent(event, sejournTime);
        processStats.storeRemainingTimeForEvent(event, remainingTime);
        processStats.storeElapseTimeForEvent(event, elapseTime);

        log.debug(String.format("Processing event %s %d %d",event.getEventKey(), sejournTime, remainingTime));

        storeEvent(event);
        nbEventsProcessed++;
    }

    void storeEvent(FlowNodeCompletedEvent event) {

        // Keep track of latest event for the process instance, for sejourn time computation of next event
        processInstanceEvents.put(event.getRootProcessInstanceId(), event);
    }

    /*
    * Sejourn time is the time spent in a particular state (expressed in miliseconds).
    * The sejourn time is hence the time difference between 2 flow node completion time.
    * As the sejourn time requires 2 processInstanceEvents to be computed, the sejourn time is 0 for the creation of the flownode (initial event)
     */
    public Long computeSejournTime(FlowNodeCompletedEvent previousEvent, FlowNodeCompletedEvent event) {
        if(previousEvent==null) {
            return event.getCompletionTime() - startDate;
        }

        long time = event.getCompletionTime() - previousEvent.getCompletionTime();

        return time;
    }

    public long getNumberProcessedEvents() {
        return nbEventsProcessed;
    }

    public long computeRemainingTime(FlowNodeCompletedEvent event) {
        return completionDate - event.getCompletionTime();
    }

    public long computeElapseTime(FlowNodeCompletedEvent event) {
        return event.getCompletionTime() - startDate;
    }
}
