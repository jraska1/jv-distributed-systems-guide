package cz.dsw.distribguide.example04.route;

import cz.dsw.distribguide.example04.entity.*;

import org.apache.camel.ExchangeTimedOutException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.GroupedBodyAggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class CamelRoutes extends RouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(CamelRoutes.class);

    @Value("${routes.providers.switchOn:true}")
    private boolean providerOn;

    @Override
    public void configure() {

//      Applicant Route definitions ...
        from("direct:applicant01").routeId("applicant01")
//                .to("activemq:queue:QUEUE-1?explicitQosEnabled=true&timeToLive={{routes.applicant01.ttl:-1}}")
            .to("activemq:queue:QUEUE-1?preserveMessageQos=true");

        from("direct:applicant02").routeId("applicant02")
            .multicast()
//                    .to("activemq:queue:QUEUE-1?explicitQosEnabled=true&timeToLive={{routes.applicant02.ttl:-1}}",
//                        "activemq:queue:QUEUE-2?explicitQosEnabled=true&timeToLive={{routes.applicant02.ttl:-1}}",
//                        "activemq:queue:QUEUE-3?explicitQosEnabled=true&timeToLive={{routes.applicant02.ttl:-1}}")
                .to("activemq:queue:QUEUE-1?preserveMessageQos=true",
                    "activemq:queue:QUEUE-2?preserveMessageQos=true",
                    "activemq:queue:QUEUE-3?preserveMessageQos=true")
            .end();

        from("direct:applicant03").routeId("applicant03")
            .to("activemq:queue:QUEUE-1?explicitQosEnabled=true&requestTimeoutCheckerInterval={{routes.applicants.checkerInterval}}");

        from("direct:applicant04").routeId("applicant04")
            .multicast()
                .aggregationStrategy(new GroupedBodyAggregationStrategy())
                .to("activemq:queue:QUEUE-1?explicitQosEnabled=true&requestTimeoutCheckerInterval={{routes.applicants.checkerInterval}}",
                    "activemq:queue:QUEUE-2?explicitQosEnabled=true&requestTimeoutCheckerInterval={{routes.applicants.checkerInterval}}",
                    "activemq:queue:QUEUE-3?explicitQosEnabled=true&requestTimeoutCheckerInterval={{routes.applicants.checkerInterval}}")
            .end();

        from("direct:applicant05").routeId("applicant05")
            .onException(ExchangeTimedOutException.class)
                .handled(true)
                .process(exchange -> {
                    logger.warn("TIMEOUT Exception handled");
                    exchange.getMessage().setBody(null);
                })
            .end()
            .multicast()
                .parallelProcessing()
                .aggregationStrategy(new GroupedBodyAggregationStrategy())
                .to("activemq:queue:QUEUE-1?explicitQosEnabled=true&requestTimeoutCheckerInterval={{routes.applicants.checkerInterval}}",
                    "activemq:queue:QUEUE-2?explicitQosEnabled=true&requestTimeoutCheckerInterval={{routes.applicants.checkerInterval}}",
                    "activemq:queue:QUEUE-3?explicitQosEnabled=true&requestTimeoutCheckerInterval={{routes.applicants.checkerInterval}}")
            .end();


//      Provider Route definitions ...
        if (providerOn) {
            from("activemq:queue:QUEUE-1").routeId("provider01")
                .process(exchange -> {
                    Request request = exchange.getMessage().getBody(Request.class);
                    logger.info(">>> {}", request);
                    Response response = new Response("provider01", new Date(), request.getValue() + 10);
                    TimeUnit.MILLISECONDS.sleep(1500);
                    logger.info("<<< {}", response);
                    exchange.getMessage().setBody(response);
                });

            from("activemq:queue:QUEUE-2").routeId("provider02")
                .process(exchange -> {
                    Request request = exchange.getMessage().getBody(Request.class);
                    logger.info(">>> {}", request);
                    Response response = new Response("provider02", new Date(), (request.getValue() + 10) * 2);
                    TimeUnit.MILLISECONDS.sleep(500);
                    logger.info("<<< {}", response);
                    exchange.getMessage().setBody(response);
                });

            from("activemq:queue:QUEUE-3").routeId("provider03")
                .process(exchange -> {
                    Request request = exchange.getMessage().getBody(Request.class);
                    logger.info(">>> {}", request);
                    Response response = new Response("provider03", new Date(), (request.getValue() + 50) * request.getValue());
                    TimeUnit.MILLISECONDS.sleep(2500);
                    logger.info("<<< {}", response);
                    exchange.getMessage().setBody(response);
                });
        }
    }
}
