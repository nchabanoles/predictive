package predictive.event;

/**
 * Created by Nicolas Chabanoles on 14/04/17.
 */
public class FlowNodeEventLog {

    private final long timeStamp;

    private final long id;

    private final String name;

    private final long rootProcessInstanceId;

    private final String stateName;

    private final long userId;

    public FlowNodeEventLog(long timeStamp, long id, String name, long rootProcessInstanceId, String stateName, long userId) {
        this.timeStamp = timeStamp;
        this.id = id;
        this.name = name;
        this.rootProcessInstanceId = rootProcessInstanceId;
        this.stateName = stateName;
        this.userId = userId;
    }

    public long getId() {
        return id;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getName() {
        return name;
    }


    @Override
    public String toString() {
        return String.format(
                "FlowNode[timeStamp=%d, id=%d, name=%s, rootProcessInstanceId=%d, stateName=%s, userId=%s]",
                timeStamp, id, name, rootProcessInstanceId, stateName, userId);
    }
}
