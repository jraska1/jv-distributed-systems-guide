package cz.dsw.distribguide.example04.rest;

import cz.dsw.distribguide.example04.entity.*;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.jms.JmsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ServiceController {

    private static final String JMS_EXPIRATION = "JMSExpiration";

    @Autowired
    private ProducerTemplate producerTemplate;

    @RequestMapping(value = "/mesg/appl01")
    public void sendMessageApplicant01(@RequestBody Request request, @RequestParam(value = "ttl", required = false, defaultValue = "-1") Long ttl) {
        if (request.getName() == null)
            request.setName("mesg-applicant01");
        request.setTs(new Date());

        Map<String, Object> headers = new HashMap<>();
        if (ttl >= 0)
            headers.put(JMS_EXPIRATION, System.currentTimeMillis() + ttl);

        producerTemplate.sendBodyAndHeaders("direct:applicant01", request, headers);
    }

    @RequestMapping(value = "/mesg/appl02")
    public void sendMessageApplicant02(@RequestBody Request request, @RequestParam(value = "ttl", required = false, defaultValue = "-1") Long ttl) {
        if (request.getName() == null)
            request.setName("mesg-applicant02");
        request.setTs(new Date());

        Map<String, Object> headers = new HashMap<>();
        if (ttl >= 0)
            headers.put(JMS_EXPIRATION, System.currentTimeMillis() + ttl);

        producerTemplate.sendBodyAndHeaders("direct:applicant02", request, headers);
    }

    @RequestMapping(value = "/rest/appl01")
    public ResponseEntity<Response> restApplicant01(@RequestBody Request request, @RequestParam(value = "expire", required = false, defaultValue = "20000") Long expire) {
        if (request.getName() == null)
            request.setName("rest-applicant01");
        request.setTs(new Date());

        Response response = producerTemplate.requestBodyAndHeader("direct:applicant03", request, JmsConstants.JMS_REQUEST_TIMEOUT, expire, Response.class);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/rest/appl02")
    public ResponseEntity<List<Response>> restApplicant02(@RequestBody Request request, @RequestParam(value = "expire", required = false, defaultValue = "20000") Long expire) {
        if (request.getName() == null)
            request.setName("rest-applicant02");
        request.setTs(new Date());

        List<Response> response = producerTemplate.requestBodyAndHeader("direct:applicant04", request, JmsConstants.JMS_REQUEST_TIMEOUT, expire, List.class);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/rest/appl03")
    public ResponseEntity<List<Response>> restApplicant03(@RequestBody Request request, @RequestParam(value = "expire", required = false, defaultValue = "20000") Long expire) {
        if (request.getName() == null)
            request.setName("rest-applicant03");
        request.setTs(new Date());

        List<Response> response = producerTemplate.requestBodyAndHeader("direct:applicant05", request, JmsConstants.JMS_REQUEST_TIMEOUT, expire, List.class);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}