package cz.dsw.distribguide.example01.route;

import cz.dsw.distribguide.example01.entity.*;

import org.apache.camel.builder.RouteBuilder;
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
        from("timer://applicant01?fixedRate=true&delay=0&repeatCount=1").routeId("applicant01")
            .process(exchange -> exchange.getMessage().setBody(new Request("applicant01", new Date(), 10)))
            .to("activemq:queue:QUEUE-1");

        from("timer://applicant02?fixedRate=true&delay=10000&repeatCount=1").routeId("applicant02")
            .process(exchange -> exchange.getMessage().setBody(new Request("applicant02", new Date(), 20)))
            .multicast()
                .to("activemq:queue:QUEUE-1", "activemq:queue:QUEUE-2", "activemq:queue:QUEUE-3")
            .end();

        from("timer://applicant03?fixedRate=true&delay=20000&repeatCount=1").routeId("applicant03")
            .process(exchange -> exchange.getMessage().setBody(new Request("applicant03", new Date(), 30)))
            .to("activemq:topic:TOPIC");

//      Provider Route definitions ...
        from("activemq:queue:QUEUE-1").routeId("provider01")
            .process(exchange -> logger.info("... {}", exchange.getMessage().getBody(Request.class)));

        from("activemq:queue:QUEUE-2").routeId("provider02")
            .process(exchange -> logger.info("... {}", exchange.getMessage().getBody(Request.class)));

        from("activemq:queue:QUEUE-3").routeId("provider03")
            .process(exchange -> logger.info("... {}", exchange.getMessage().getBody(Request.class)));

        from("activemq:topic:TOPIC").routeId("provider04")
            .process(exchange -> logger.info("... {}", exchange.getMessage().getBody(Request.class)));

        from("activemq:topic:TOPIC").routeId("provider05")
            .process(exchange -> logger.info("... {}", exchange.getMessage().getBody(Request.class)));

        from("activemq:topic:TOPIC").routeId("provider06")
            .process(exchange -> logger.info("... {}", exchange.getMessage().getBody(Request.class)));
    }
}
