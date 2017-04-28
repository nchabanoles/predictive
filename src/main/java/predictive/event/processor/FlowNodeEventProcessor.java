package predictive.event.processor;

import java.util.function.Consumer;

import predictive.event.FlowNodeCompletedEvent;
import predictive.event.processor.collectors.ProcessStats;
import predictive.event.processor.collectors.StatsCollector;

/**
 * Created by Nicolas Chabanoles on 14/04/17.
 */
public class FlowNodeEventProcessor implements Consumer<FlowNodeCompletedEvent>{


    final private StatsCollector stats;

    public FlowNodeEventProcessor(ProcessStats processStats, long processInstanceStartTime, long processInstanceCompletionTime) {
        stats = new StatsCollector(processStats, processInstanceStartTime, processInstanceCompletionTime);
    }

    @Override
    public void accept(FlowNodeCompletedEvent event) {
       stats.accept(event);
    }

    public long getNumberProcessedEvents() {
        return stats.getNumberProcessedEvents();
    }
}
