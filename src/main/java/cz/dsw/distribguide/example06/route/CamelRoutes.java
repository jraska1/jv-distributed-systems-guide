package cz.dsw.distribguide.example06.route;

import cz.dsw.distribguide.example06.entity.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class CamelRoutes extends RouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(CamelRoutes.class);

    private static final String HEADER_CLASS_NAME = "ObjectClassName";

    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Override
    public void configure() {

//      Serialization Route definitions ...
        from("direct:object-mapping").routeId("object-mapping")
            .choice()
                .when(simple("${header.ObjectMapping?.toLowerCase()} == 'json'"))
                    .process(exchange -> {
                        Token token = exchange.getMessage().getBody(Token.class);
                        exchange.getMessage().setHeader(HEADER_CLASS_NAME, token.getClass().getCanonicalName());
                        String json = jsonMapper.writeValueAsString(exchange.getMessage().getBody());
                        exchange.getMessage().setBody(json, String.class);
                        logger.info("SERIALIZED OBJECT: {}", json);
                    })
                .otherwise()
                    .process(exchange -> logger.info("No object mapping applied on the message."))
            .end();

        from("direct:reverse-mapping").routeId("reverse-mapping")
            .choice()
                .when(simple("${header.ObjectMapping?.toLowerCase()} == 'json'"))
                    .process(exchange -> {
                        String className = exchange.getMessage().getHeader(HEADER_CLASS_NAME, String.class);
                        Token token = (Token) jsonMapper.readValue(exchange.getMessage().getBody(String.class), Class.forName(className));
                        exchange.getMessage().setBody(token);
                        logger.info("DESERIALIZED OBJECT: {}", exchange.getMessage().getBody(String.class));
                    })
                .otherwise()
                    .process(exchange -> logger.debug("No reverse mapping applied on the message."))
            .end();

//      Applicant Route definitions ...
        from("direct:applicant01").routeId("applicant01")
            .to("direct:object-mapping")
            .multicast()
                .aggregationStrategy((oldExchange, newExchange) -> {
                    Exchange result;

                    producerTemplate.send("direct:reverse-mapping", newExchange);

                    List<Response> list;
                    if (oldExchange != null) {
                        list = oldExchange.getIn().getBody(List.class);
                        result = oldExchange;
                    }
                    else {
                        list = new ArrayList<>();
                        result = newExchange;
                    }
                    Response resp = newExchange.getMessage().getBody(Response.class);
                    list.add(newExchange.getMessage().getBody(Response.class));
                    result.getMessage().setBody(list, List.class);
                    return result;
                })
                .to("activemq:queue:QUEUE-1", "activemq:queue:QUEUE-2", "activemq:queue:QUEUE-3")
            .end();

//      Provider Route definitions ...
        from("activemq:queue:QUEUE-1").routeId("provider01")
            .to("direct:reverse-mapping")
            .process(exchange -> {
                Request request = exchange.getMessage().getBody(Request.class);
                Response response = new Response("provider01", new Date(), request.getValue() + 10);
                exchange.getMessage().setBody(response);
            })
            .to("direct:object-mapping");

        from("activemq:queue:QUEUE-2").routeId("provider02")
            .to("direct:reverse-mapping")
            .process(exchange -> {
                Request request = exchange.getMessage().getBody(Request.class);
                Response response = new Response("provider02", new Date(), (request.getValue() + 10) * 2);
                exchange.getMessage().setBody(response);
            })
            .to("direct:object-mapping");

        from("activemq:queue:QUEUE-3").routeId("provider03")
            .to("direct:reverse-mapping")
            .process(exchange -> {
                Request request = exchange.getMessage().getBody(Request.class);
                Response response = new Response("provider03", new Date(), (request.getValue() + 50) * request.getValue());
                exchange.getMessage().setBody(response);
            })
            .to("direct:object-mapping");
    }
}
