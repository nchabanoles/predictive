package predictive.event.processor.collectors

import predictive.event.FlowNodeCompletedEvent
import spock.lang.Specification

/**
 * Created by Nicolas Chabanoles on 24/04/17.
 */
class StatsCollectorTest extends Specification {
    def "Stats Collector should count the number of processed events"() {

        given:
        StatsCollector stats = new StatsCollector(new ProcessStats(), 1234L, 2468L)
        FlowNodeCompletedEvent event = new FlowNodeCompletedEvent(1L, "aProcess-aStep", 2L, 3L)

        expect:
        0 == stats.getNumberProcessedEvents()

        when:
        stats.accept(event)
        stats.accept(event)

        then:
        2 == stats.getNumberProcessedEvents()
    }

    def "Stats should ignore null events"() {

        given:
        StatsCollector stats = new StatsCollector(new ProcessStats(),1234L, 2468L)

        expect:
        0 == stats.getNumberProcessedEvents()

        when:
        stats.accept(null)

        then:
        0 == stats.getNumberProcessedEvents()
    }

    def "Sejourn time of first event should be completionDate - CaseStartDate"() {

        given:
        StatsCollector stats = new StatsCollector(new ProcessStats(), 1234L, 2468L)
        FlowNodeCompletedEvent event = new FlowNodeCompletedEvent(1334L, "aProcess-aStep", 2L, 3L)

        when:
        long sejournTime = stats.computeSejournTime(null, event)

        then:
        (1334L - 1234L) == sejournTime
    }

    def "Sejourn time of events should be the time difference with previous event"() {

        given:
        StatsCollector stats = new StatsCollector(new ProcessStats(), 1234L, 2468L)
        FlowNodeCompletedEvent previousEvent = new FlowNodeCompletedEvent(100L, "aProcess-aStep", 2L, 3L)
        FlowNodeCompletedEvent event = new FlowNodeCompletedEvent(300L, "aProcess-aStep", 2L, 3L)

        when:
        long sejournTime = stats.computeSejournTime(previousEvent, event)

        then:
        200 == sejournTime // Time difference between completion time. 300L - 200L
    }

    def "Remaining time of first event should be the time difference between case completion and its completion time"() {

        given:
        StatsCollector stats = new StatsCollector(new ProcessStats(), 1234L, 2468L)
        FlowNodeCompletedEvent event = new FlowNodeCompletedEvent(300L, "anotherProcess-anotherStep", 2L, 3L)

        when:
        long remainingTime = stats.computeRemainingTime(event)

        then:
        (2468L - 300L) == remainingTime // Time difference between completion time. 300L - 200L
    }


    def "Elapse time of first event should be sejourn time of same event"() {

        given:
        StatsCollector stats = new StatsCollector(new ProcessStats(), 1234L, 2468L)
        FlowNodeCompletedEvent event = new FlowNodeCompletedEvent(1334L, "aProcess-aStep", 2L, 3L)

        when:
        long time = stats.computeElapseTime(event)

        then:
        (1334L - 1234L) == time
    }


}
