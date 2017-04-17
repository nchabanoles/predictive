package predictive.event.processor

import org.junit.Assert
import predictive.event.FlowNodeEventLog

/**
 * Created by Nicolas Chabanoles on 16/04/2017.
 */
class FlowNodeEventProcessorTest extends spock.lang.Specification {
    def "Processor should count the number of processed events"() {

        given:
        FlowNodeEventProcessor processor = new FlowNodeEventProcessor()
        FlowNodeEventLog event = new FlowNodeEventLog(0L, 1L, "anEvent", 2L, "aState", 3L)

        expect:
        0 == processor.getNumberProcessedEvents()

        when:
        processor.accept(event)

        then:
        1 == processor.getNumberProcessedEvents()
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
