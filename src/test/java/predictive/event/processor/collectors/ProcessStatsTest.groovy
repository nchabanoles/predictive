package predictive.event.processor.collectors

import predictive.event.FlowNodeCompletedEvent
import spock.lang.Specification

/**
 * Created by bonita on 24/04/17.
 */
class ProcessStatsTest extends Specification {
    def "Should store remaining times per eventKey"() {
        given:
        ProcessStats stats = new ProcessStats()
        FlowNodeCompletedEvent firstEvent = new FlowNodeCompletedEvent(1L, "sameProcess-sameStep", 2L, 3L)
        FlowNodeCompletedEvent secondEvent = new FlowNodeCompletedEvent(1L, "sameProcess-sameStep", 2L, 3L)
        FlowNodeCompletedEvent anotherEvent = new FlowNodeCompletedEvent(1L, "differentProcess-differentStep", 2L, 3L)

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

    def "PrintStats"() {
    }

    def "StoreSejournTime"() {
    }
}
