package predictive.event;

public class ProcessInstanceEventLog {
    private long id;

    public ProcessInstanceEventLog(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format(
                "Case[id=%d]",
                id);
    }

    // getters & setters omitted for brevity
}