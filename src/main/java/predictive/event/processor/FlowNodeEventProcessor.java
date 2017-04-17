package predictive.event.processor;

import java.io.PrintStream;
import java.util.function.Consumer;

import predictive.event.FlowNodeEventLog;
import predictive.event.processor.collectors.StatsCollector;

/**
 * Created by Nicolas Chabanoles on 14/04/17.
 */
public class FlowNodeEventProcessor implements Consumer<FlowNodeEventLog>{


    private StatsCollector stats = new StatsCollector();


    @Override
    public void accept(FlowNodeEventLog event) {
       stats.accept(event);
    }

    public long getNumberProcessedEvents() {
        return stats.getNumberProcessedEvents();
    }
}
