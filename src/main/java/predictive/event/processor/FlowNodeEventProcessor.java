package predictive.event.processor;

import java.util.function.Consumer;

import predictive.event.FlowNodeEventLog;

/**
 * Created by Nicolas Chabanoles on 14/04/17.
 */
public class FlowNodeEventProcessor implements Consumer<FlowNodeEventLog>{

    @Override
    public void accept(FlowNodeEventLog event) {
        System.err.println(event.toString());
    }
}
