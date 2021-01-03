package cz.dsw.distribguide.example09.route;

import cz.dsw.distribguide.example09.entity.*;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.GroupedBodyAggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class CamelRoutes extends RouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(CamelRoutes.class);

    @Override
    public void configure() {

//      Applicant Route definitions ...
        from("direct:applicant01").routeId("applicant01")
            .multicast()
                .aggregationStrategy(new GroupedBodyAggregationStrategy())
                .to("activemq:queue:QUEUE-1", "activemq:queue:QUEUE-2", "activemq:queue:QUEUE-3")
            .end();

        from("direct:applicant02").routeId("applicant02")
            .multicast()
                .aggregationStrategy((oldExchange, newExchange) -> {
                    Exchange result;
                    List<ResponseBasis> list;
                    if (oldExchange != null) {
                        list = oldExchange.getIn().getBody(List.class);
                        result = oldExchange;
                    }
                    else {
                        list = new ArrayList<>();
                        result = newExchange;
                    }
                    ResponseBasis resp = newExchange.getMessage().getBody(ResponseBasis.class);
                    if (resp.getCode() == ResponseCodeType.OK) {
                        list.add(newExchange.getMessage().getBody(ResponseBasis.class));
                    }
                    result.getMessage().setBody(list, List.class);
                    return result;
                })
                .to("activemq:queue:QUEUE-1", "activemq:queue:QUEUE-2", "activemq:queue:QUEUE-3")
            .end();

//      Provider Route definitions ...
        from("activemq:queue:QUEUE-1").routeId("provider01")
            .choice()
                .when(body().isInstanceOf(RequestA.class))
                    .process(exchange -> {
                        RequestA request = exchange.getMessage().getBody(RequestA.class);
                        ResponseA response = new ResponseA("provider01", new Date(), ResponseCodeType.OK,request.getValue() + 10);
                        exchange.getMessage().setBody(response);
                    })
                .when(body().isInstanceOf(RequestB.class))
                    .process(exchange -> {
                        RequestB request = exchange.getMessage().getBody(RequestB.class);
                        ResponseB response = new ResponseB("provider01", new Date(), ResponseCodeType.OK, "text length: " + request.getText().length());
                        exchange.getMessage().setBody(response);
                    })
                .otherwise()
                    .process(exchange -> {
                        ResponseBasis response = new ResponseBasis("provider01", new Date(), ResponseCodeType.REFUSED);
                        exchange.getMessage().setBody(response);
                    })
            .end();

        from("activemq:queue:QUEUE-2").routeId("provider02")
            .choice()
                .when(body().isInstanceOf(RequestA.class))
                    .process(exchange -> {
                        RequestA request = exchange.getMessage().getBody(RequestA.class);
                        ResponseA response = new ResponseA("provider02", new Date(), ResponseCodeType.OK,(request.getValue() + 10) * 2);
                        exchange.getMessage().setBody(response);
                    })
                .otherwise()
                    .process(exchange -> {
                        ResponseBasis response = new ResponseBasis("provider02", new Date(), ResponseCodeType.REFUSED);
                        exchange.getMessage().setBody(response);
                    })
            .end();

        from("activemq:queue:QUEUE-3").routeId("provider03")
            .choice()
                .when(body().isInstanceOf(RequestB.class))
                    .process(exchange -> {
                        RequestB request = exchange.getMessage().getBody(RequestB.class);
                        ResponseB response = new ResponseB("provider03", new Date(), ResponseCodeType.OK, ">>> " + request.getText() + " <<<");
                        exchange.getMessage().setBody(response);
                    })
                .otherwise()
                    .process(exchange -> {
                        ResponseBasis response = new ResponseBasis("provider03", new Date(), ResponseCodeType.REFUSED);
                        exchange.getMessage().setBody(response);
                    })
            .end();
    }
}
