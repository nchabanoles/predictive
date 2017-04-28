package predictive.event;

/**
 * Created by Nicolas Chabanoles on 14/04/17.
 */
public class FlowNodeCompletedEvent {

    private final long completionTime;

    private final String eventKey;

    private final long rootProcessInstanceId;

    private final long userId;

    public FlowNodeCompletedEvent(long completionTime, String eventKey, long rootProcessInstanceId, long userId) {
        this.completionTime = completionTime;
        this.eventKey = eventKey;
        this.rootProcessInstanceId = rootProcessInstanceId;
        this.userId = userId;
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public String getEventKey() {
        return eventKey;
    }

    public long getRootProcessInstanceId() {
        return rootProcessInstanceId;
    }

    @Override
    public String toString() {
        return String.format(
                "FlowNode[completionTime=%d, eventKey=%s, rootProcessInstanceId=%d, userId=%s]",
                completionTime, eventKey, rootProcessInstanceId, userId);
    }
}
