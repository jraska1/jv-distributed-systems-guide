package cz.dsw.distribguide.example03.route;

import cz.dsw.distribguide.example03.entity.*;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.GroupedBodyAggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class CamelRoutes extends RouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(CamelRoutes.class);

    @Override
    public void configure() {

//      Applicant Route definitions ...
        from("direct:applicant01").routeId("applicant01")
            .to("activemq:queue:QUEUE-1");

        from("direct:applicant02").routeId("applicant02")
            .multicast()
                .aggregationStrategy(new GroupedBodyAggregationStrategy())
                .to("activemq:queue:QUEUE-1", "activemq:queue:QUEUE-2", "activemq:queue:QUEUE-3")
            .end();

//      Provider Route definitions ...
        from("activemq:queue:QUEUE-1").routeId("provider01")
            .process(exchange -> {
                Request request = exchange.getMessage().getBody(Request.class);
                logger.info("... {}", request);
                exchange.getMessage().setBody(new Response("provider01", new Date(), request.getValue() + 10));
            });

        from("activemq:queue:QUEUE-2").routeId("provider02")
            .process(exchange -> {
                Request request = exchange.getMessage().getBody(Request.class);
                logger.info("... {}", request);
                exchange.getMessage().setBody(new Response("provider02", new Date(), (request.getValue() + 10) * 2));
            });

        from("activemq:queue:QUEUE-3").routeId("provider03")
            .process(exchange -> {
                Request request = exchange.getMessage().getBody(Request.class);
                logger.info("... {}", request);
                exchange.getMessage().setBody(new Response("provider03", new Date(), (request.getValue() + 50) * request.getValue()));
            });
    }
}
