package cz.dsw.distribguide.example02.route;

import cz.dsw.distribguide.example02.entity.*;

import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.GroupedBodyAggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class CamelRoutes extends RouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(CamelRoutes.class);

    @Override
    public void configure() {

//      Applicant Route definitions ...
        from("timer://applicant01?fixedRate=true&delay=0&repeatCount=1").routeId("applicant01")
            .process(exchange -> {
                exchange.getMessage().setBody(new Request("applicant01", new Date(), 10));
                logger.info(">>> {}", exchange.getMessage().getBody(Request.class));
            })
            .setExchangePattern(ExchangePattern.InOut)
            .to("activemq:queue:QUEUE-1")
            .process(exchange -> logger.info("<<< {}", exchange.getMessage().getBody(Response.class)));

        from("timer://applicant02?fixedRate=true&delay=10000&repeatCount=1").routeId("applicant02")
            .process(exchange -> {
                exchange.getMessage().setBody(new Request("applicant02", new Date(), 20));
                logger.info(">>> {}", exchange.getMessage().getBody(Request.class));
            })
            .setExchangePattern(ExchangePattern.InOut)
            .multicast()
                .aggregationStrategy(new GroupedBodyAggregationStrategy())
                .to("activemq:queue:QUEUE-1", "activemq:queue:QUEUE-2", "activemq:queue:QUEUE-3")
            .end()
            .process(exchange -> exchange.getMessage().getBody(List.class).forEach(o -> logger.info("<<< {}", o.toString())));

//      Provider Route definitions ...
        from("activemq:queue:QUEUE-1").routeId("provider01")
            .process(exchange -> {
                Request request = exchange.getMessage().getBody(Request.class);
                exchange.getMessage().setBody(new Response("provider01", new Date(), request.getValue() + 10));
            });

        from("activemq:queue:QUEUE-2").routeId("provider02")
            .process(exchange -> {
                Request request = exchange.getMessage().getBody(Request.class);
                exchange.getMessage().setBody(new Response("provider02", new Date(), (request.getValue() + 10) * 2));
            });

        from("activemq:queue:QUEUE-3").routeId("provider03")
            .process(exchange -> {
                Request request = exchange.getMessage().getBody(Request.class);
                exchange.getMessage().setBody(new Response("provider03", new Date(), (request.getValue() + 50) * request.getValue()));
            });
    }
}
