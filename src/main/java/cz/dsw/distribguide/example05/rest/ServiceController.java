package cz.dsw.distribguide.example05.rest;

import cz.dsw.distribguide.example05.entity.*;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.jms.JmsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ServiceController {

    private static final String RECIPIENT_LIST = "Recipients";

    @Value("${routes.applicants.checkerInterval:100}")
    private long checkerInterval;

    @Autowired
    private ProducerTemplate producerTemplate;

    @RequestMapping(value = "/rest/appl01")
    public ResponseEntity<List<Response>> restApplicant01(
            @RequestBody Request request,
            @RequestParam(value = "destination") List<String> destinations,
            @RequestParam(value = "expire", required = false, defaultValue = "5000") Long expire) {
        if (request.getName() == null)
            request.setName("rest-applicant01");
        request.setTs(new Date());

        Map<String, Object> headers = new HashMap<>();
        headers.put(RECIPIENT_LIST, destinations.stream()
                .map(s -> "activemq:queue:" + s + "?explicitQosEnabled=true&requestTimeoutCheckerInterval=" + checkerInterval)
                .collect(Collectors.toList()));
        headers.put(JmsConstants.JMS_REQUEST_TIMEOUT, expire);

        List<Response> response = producerTemplate.requestBodyAndHeaders("direct:applicant01", request, headers, List.class);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}