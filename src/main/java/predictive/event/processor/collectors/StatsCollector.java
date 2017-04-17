package predictive.event.processor.collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import predictive.event.FlowNodeEventLog;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by Nicolas Chabanoles on 17/04/2017.
 */
public class StatsCollector implements Consumer<FlowNodeEventLog> {


    private static final Logger log = LoggerFactory.getLogger(StatsCollector.class);

    private long nbEventsProcessed = 0L;

    private Map<String, FlowNodeEventLog> events = new HashMap<>();
    private Map<String, List<Long>> sejournTimes = new HashMap<>();

    @Override
    public void accept(FlowNodeEventLog event) {
        if(event==null) {
            return; // ignore null event
        }
        Long time = computeSejournTime(event);
        storeEventAndSejournTime(event, time);
        nbEventsProcessed++;

        printStats(nbEventsProcessed, event, time);
    }

    private void printStats(long nbEventsProcessed, FlowNodeEventLog event, Long time) {
        log.info(String.format("Event '%s' processed (#%d):\n\tSejourn time: %d\n\tVector: %s", event.getName(), nbEventsProcessed, time, sejournTimes.get(event.getName())));
    }

    /*
    * Events are stored to compute sejourn times. Only previous event is necessary.
    * The key identifier is the flow node name.
    * TODO: Use a unique identifier instead of name. Example: FLOWNODEDEFINITIONID, or a couple (ProcessDefinitionID, name)
     */
    private List<Long> storeEventAndSejournTime(FlowNodeEventLog event, Long time) {
        // Add sejourn time to vector of computed values
        List<Long> vector = sejournTimes.getOrDefault(event.getName(), new ArrayList<>());
        vector.add(time);
        // Make sure the vector is persisted
        sejournTimes.putIfAbsent(event.getName(),vector);

        // Keep track of latest event for the flow node, for sejourn time computation of next event
        events.put(event.getName(), event);

        return Collections.unmodifiableList(vector);
    }

    /*
    * Sejourn time is the time spent in a particular state (expressed in miliseconds).
    * The sejourn time is hence the time difference between 2 change state events for the same flow node.
    * As the sejourn time requires 2 events to be computed, the sejourn time is 0 for the creation of the flownode (initial event)
     */
    private Long computeSejournTime(FlowNodeEventLog event) {
        FlowNodeEventLog previousEvent = events.get(event.getName());
        if(previousEvent==null) {
            return 0L;
        }

        long time = event.getTimeStamp() - previousEvent.getTimeStamp();
        if(time <=0) {
            log.error(String.format("!!!!! Negative or null time difference for events %d (%d) and %d (%d)", previousEvent.getId(), previousEvent.getTimeStamp(), event.getId(), event.getTimeStamp()));
        }
        return time;
    }

    public long getNumberProcessedEvents() {
        return nbEventsProcessed;
    }
}
