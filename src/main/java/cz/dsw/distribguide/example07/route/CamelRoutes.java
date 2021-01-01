package cz.dsw.distribguide.example07.route;

import cz.dsw.distribguide.example07.entity.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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

//      Compression Route definitions ...
        from("direct:object-compression").routeId("object-compression")
            .choice()
                .when(simple("${header.ObjectCompression?.toLowerCase()} == 'gzip'"))
                    .process(exchange -> {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
                            gzipOut.write(exchange.getMessage().getBody(String.class).getBytes());
                        }
                        exchange.getMessage().setBody(baos.toByteArray(), byte[].class);
                        logger.info("COMPRESSED OBJECT to GZIP");
                    })
                .otherwise()
                    .process(exchange -> logger.info("No object compression applied on the message."))
            .end();

        from("direct:reverse-compression").routeId("reverse-compression")
            .choice()
                .when(simple("${header.ObjectCompression?.toLowerCase()} == 'gzip'"))
                    .process(exchange -> {
                        GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(exchange.getMessage().getBody(byte[].class)));
                        exchange.getMessage().setBody(new String(gzipIn.readAllBytes()));
                        logger.info("DECOMPRESSED OBJECT from GZIP");
                    })
                .otherwise()
                    .process(exchange -> logger.debug("No reverse compression applied on the message."))
            .end();

//      Serialization Route definitions ...
        from("direct:object-mapping").routeId("object-mapping")
            .process(exchange -> {
                Token token = exchange.getMessage().getBody(Token.class);
                exchange.getMessage().setHeader(HEADER_CLASS_NAME, token.getClass().getCanonicalName());
                exchange.getMessage().setBody(jsonMapper.writeValueAsString(exchange.getMessage().getBody()), String.class);
            });

        from("direct:reverse-mapping").routeId("reverse-mapping")
            .process(exchange -> {
                String className = exchange.getMessage().getHeader(HEADER_CLASS_NAME, String.class);
                Token token = (Token) jsonMapper.readValue(exchange.getMessage().getBody(String.class), Class.forName(className));
                exchange.getMessage().setBody(token);
            });

//      Applicant Route definitions ...
        from("direct:applicant01").routeId("applicant01")
            .to("direct:object-mapping")
            .to("direct:object-compression")
            .multicast()
                .aggregationStrategy((oldExchange, newExchange) -> {
                    Exchange result;

                    producerTemplate.send("direct:reverse-compression", newExchange);
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
                .to("direct:reverse-compression")
                .to("direct:reverse-mapping")
                .process(exchange -> {
                    Request request = exchange.getMessage().getBody(Request.class);
                    Response response = new Response("provider01", new Date(), request.getValue() + 10);
                    exchange.getMessage().setBody(response);
                })
                .to("direct:object-mapping")
                .to("direct:object-compression");

        from("activemq:queue:QUEUE-2").routeId("provider02")
                .to("direct:reverse-compression")
                .to("direct:reverse-mapping")
                .process(exchange -> {
                    Request request = exchange.getMessage().getBody(Request.class);
                    Response response = new Response("provider02", new Date(), (request.getValue() + 10) * 2);
                    exchange.getMessage().setBody(response);
                })
                .to("direct:object-mapping")
                .to("direct:object-compression");

        from("activemq:queue:QUEUE-3").routeId("provider03")
                .to("direct:reverse-compression")
                .to("direct:reverse-mapping")
                .process(exchange -> {
                    Request request = exchange.getMessage().getBody(Request.class);
                    Response response = new Response("provider03", new Date(), (request.getValue() + 50) * request.getValue());
                    exchange.getMessage().setBody(response);
                })
                .to("direct:object-mapping")
                .to("direct:object-compression");
    }
}
