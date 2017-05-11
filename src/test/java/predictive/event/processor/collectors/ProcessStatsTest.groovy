package predictive.event.processor.collectors

import predictive.event.FlowNodeCompletedEvent
import spock.lang.Specification

import java.util.stream.LongStream

/**
 * Created by Nicolas Chabanoles on 24/04/17.
 */
class ProcessStatsTest extends Specification {
    def "Should store remaining times per eventKey"() {
        given:
        ProcessStats stats = new ProcessStats()
        FlowNodeCompletedEvent firstEvent = new FlowNodeCompletedEvent(0L,100L,1L, "sameProcess-sameStep", 2L, 3L)
        FlowNodeCompletedEvent secondEvent = new FlowNodeCompletedEvent(0L,100L,1L, "sameProcess-sameStep", 2L, 3L)
        FlowNodeCompletedEvent anotherEvent = new FlowNodeCompletedEvent(0L,100L,1L, "differentProcess-differentStep", 2L, 3L)

        List<Long> remainingTimesAnotherEvent

        stats.storeRemainingTimeForEvent(firstEvent, 1000L)
        List<Long> remainingTimesSameEvent = stats.storeRemainingTimeForEvent(secondEvent, 500L)

        expect:
        2 == remainingTimesSameEvent.size()
        remainingTimesSameEvent.containsAll(Arrays.asList(1000L,500L))

        when:
        remainingTimesAnotherEvent = stats.storeRemainingTimeForEvent(anotherEvent, 1000L)

        then:
        1 == remainingTimesAnotherEvent.size()
        remainingTimesSameEvent.contains(1000L)
    }

    def "should compute RMSE"() {
        given:
        ArrayList<Long> dataset = Arrays.asList(21587L,57803L,28564L,35656L,26733L)
        final long mean = 27555

        expect:
        dataset.size() == 5

        when:
        long sum = dataset.stream().reduce(0l, {acc, i -> acc + (Math.pow(mean - i,2))})

        then:
        sum == 1017878494L
    }

}
