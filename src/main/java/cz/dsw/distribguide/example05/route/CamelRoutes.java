package cz.dsw.distribguide.example05.route;

import cz.dsw.distribguide.example05.entity.*;

import org.apache.camel.ExchangeTimedOutException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.GroupedBodyAggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class CamelRoutes extends RouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(CamelRoutes.class);

    private static final String RECIPIENT_LIST = "Recipients";

    @Override
    public void configure() {

//      Applicant Route definitions ...
        from("direct:applicant01").routeId("applicant01")
            .onException(ExchangeTimedOutException.class)
                .handled(true)
                .process(exchange -> {
                    logger.warn("TIMEOUT Exception handled");
                    exchange.getMessage().setBody(null);
                })
            .end()
            .recipientList()
                .header(RECIPIENT_LIST)
                .parallelProcessing()
                .aggregationStrategy(new GroupedBodyAggregationStrategy())
            .end();


//      Provider Route definitions ...
        from("activemq:queue:QUEUE-1").routeId("provider01")
            .process(exchange -> {
                Request request = exchange.getMessage().getBody(Request.class);
                logger.info(">>> {}", request);
                TimeUnit.MILLISECONDS.sleep(1500);
                Response response = new Response("provider01", new Date(), request.getValue() + 10);
                logger.info("<<< {}", response);
                exchange.getMessage().setBody(response);
            });

        from("activemq:queue:QUEUE-2").routeId("provider02")
            .process(exchange -> {
                Request request = exchange.getMessage().getBody(Request.class);
                logger.info(">>> {}", request);
                TimeUnit.MILLISECONDS.sleep(500);
                Response response = new Response("provider02", new Date(), (request.getValue() + 10) * 2);
                logger.info("<<< {}", response);
                exchange.getMessage().setBody(response);
            });

        from("activemq:queue:QUEUE-3").routeId("provider03")
            .process(exchange -> {
                Request request = exchange.getMessage().getBody(Request.class);
                logger.info(">>> {}", request);
                TimeUnit.MILLISECONDS.sleep(2500);
                Response response = new Response("provider03", new Date(), (request.getValue() + 50) * request.getValue());
                logger.info("<<< {}", response);
                exchange.getMessage().setBody(response);
            });
    }
}
