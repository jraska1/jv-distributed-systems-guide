package cz.dsw.distribguide.example08.route;

import cz.dsw.distribguide.example08.entity.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.crypto.DigitalSignatureConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class CamelRoutes extends RouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(CamelRoutes.class);

    private static final String HEADER_CLASS_NAME   = "ObjectClassName";
    private static final String HEADER_SIGNATURE    = "ObjectDigitalSignature";
    private static final String HEADER_NODE_NAME    = "NodeName";

    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    Map<String, KeyPair> keys;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Override
    public void configure() {

//      Signing Route definitions ...
        from("direct:object-sign").routeId("object-sign")
            .process(exchange -> {
                KeyPair keyPair = keys.get(exchange.getMessage().getHeader(HEADER_NODE_NAME, String.class));
                if (keyPair != null)
                    exchange.getMessage().setHeader(DigitalSignatureConstants.SIGNATURE_PRIVATE_KEY, keyPair.getPrivate());
                logger.info("SIGN OBJECT");
            })
            .to("crypto:sign://basic")
            .process(exchange -> exchange.getMessage().setHeader(HEADER_SIGNATURE, exchange.getMessage().getHeader(DigitalSignatureConstants.SIGNATURE, String.class)));

        from("direct:object-verify").routeId("object-verify")
            .process(exchange -> {
                exchange.getMessage().setHeader(DigitalSignatureConstants.SIGNATURE, exchange.getMessage().getHeader(HEADER_SIGNATURE, String.class));
                KeyPair keyPair = keys.get(exchange.getMessage().getHeader(HEADER_NODE_NAME, String.class));
                if (keyPair != null)
                    exchange.getMessage().setHeader(DigitalSignatureConstants.SIGNATURE_PUBLIC_KEY_OR_CERT, keyPair.getPublic());
                logger.info("VERIFY OBJECT");
            })
            .to("crypto:verify://basic");

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
            .process(exchange -> exchange.getMessage().setHeader(HEADER_NODE_NAME, exchange.getMessage().getBody(Token.class).getName()))
            .to("direct:object-mapping")
            .to("direct:object-sign")
            .multicast()
                .aggregationStrategy((oldExchange, newExchange) -> {
                    Exchange result;

                    producerTemplate.send("direct:object-verify", newExchange);
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
                .to("direct:object-verify")
                .to("direct:reverse-mapping")
                .process(exchange -> {
                    Request request = exchange.getMessage().getBody(Request.class);
                    Response response = new Response("provider01", new Date(), request.getValue() + 10);
                    exchange.getMessage().setBody(response);
                })
                .process(exchange -> exchange.getMessage().setHeader(HEADER_NODE_NAME, exchange.getMessage().getBody(Token.class).getName()))
                .to("direct:object-mapping")
                .to("direct:object-sign");

        from("activemq:queue:QUEUE-2").routeId("provider02")
                .to("direct:object-verify")
                .to("direct:reverse-mapping")
                .process(exchange -> {
                    Request request = exchange.getMessage().getBody(Request.class);
                    Response response = new Response("provider02", new Date(), (request.getValue() + 10) * 2);
                    exchange.getMessage().setBody(response);
                })
                .process(exchange -> exchange.getMessage().setHeader(HEADER_NODE_NAME, exchange.getMessage().getBody(Token.class).getName()))
                .to("direct:object-mapping")
                .to("direct:object-sign");

        from("activemq:queue:QUEUE-3").routeId("provider03")
                .to("direct:object-verify")
                .to("direct:reverse-mapping")
                .process(exchange -> {
                    Request request = exchange.getMessage().getBody(Request.class);
                    Response response = new Response("provider03", new Date(), (request.getValue() + 50) * request.getValue());
                    exchange.getMessage().setBody(response);
                })
                .process(exchange -> exchange.getMessage().setHeader(HEADER_NODE_NAME, exchange.getMessage().getBody(Token.class).getName()))
                .to("direct:object-mapping")
                .to("direct:object-sign");
    }
}
