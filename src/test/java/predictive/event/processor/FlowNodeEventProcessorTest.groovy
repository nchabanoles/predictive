package predictive.event.processor

import predictive.event.FlowNodeCompletedEvent

/**
 * Created by Nicolas Chabanoles on 16/04/2017.
 */
class FlowNodeEventProcessorTest extends spock.lang.Specification {
    def "Processor should count the number of processed events"() {

        given:
        FlowNodeEventProcessor processor = new FlowNodeEventProcessor()
        FlowNodeCompletedEvent event = new FlowNodeCompletedEvent(1L, "anEvent", 2L, 3L)

        expect:
        0 == processor.getNumberProcessedEvents()

        when:
        processor.accept(event)
        processor.accept(event)
        processor.accept(event)

        then:
        3 == processor.getNumberProcessedEvents()
    }

    def "Processor should ignore null events"() {

        given:
        FlowNodeEventProcessor processor = new FlowNodeEventProcessor()

        expect:
        0 == processor.getNumberProcessedEvents()

        when:
        processor.accept(null)

        then:
        0 == processor.getNumberProcessedEvents()
    }
}
