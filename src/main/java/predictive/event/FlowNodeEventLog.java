package predictive.event;

/**
 * Created by Nicolas Chabanoles on 14/04/17.
 */
public class FlowNodeEventLog {

    private final long id;

    public FlowNodeEventLog(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format(
                "FlowNode[id=%d]",
                id);
    }
}
