package predictive.event;

public class ProcessInstanceEventLog {
    private long timeStamp;
    private long id;
    private long processDefinitionId;
    private long initiatorUserId;
    private long startTime;
    private long completionTime;

    public ProcessInstanceEventLog(long timeStamp, long id, long processDefinitionId, long initiatorUserId, long startTime, long completionTime) {
        this.timeStamp = timeStamp;
        this.id = id;
        this.processDefinitionId = processDefinitionId;
        this.initiatorUserId = initiatorUserId;
        this.startTime = startTime;
        this.completionTime = completionTime;
    }



    @Override
    public String toString() {
        return String.format(
                "Case[id=%d]",
                id);
    }

    public Long getId() {
        return id;
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public long getStartTime() {
        return startTime;
    }
}