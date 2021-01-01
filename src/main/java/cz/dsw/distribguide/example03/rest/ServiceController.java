package cz.dsw.distribguide.example03.rest;

import cz.dsw.distribguide.example03.entity.Request;
import cz.dsw.distribguide.example03.entity.Response;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ServiceController {

    @Autowired
    private ProducerTemplate producerTemplate;

    @RequestMapping(value = "/mesg/appl01")
    public void sendApplicant01(@RequestParam(value = "value") long value) {
        Request request = new Request("mesg-applicant01", new Date(), value);
        producerTemplate.sendBody("direct:applicant01", request);
    }

    @RequestMapping(value = "/mesg/appl02")
    public void sendApplicant02(@RequestParam(value = "value") long value) {
        Request request = new Request("mesg-applicant02", new Date(), value);
        producerTemplate.sendBody("direct:applicant02", request);
    }

    @RequestMapping(value = "/call/appl01")
    public String callApplicant01(@RequestParam(value = "value") long value) {
        Request request = new Request("call-applicant01", new Date(), value);
        Response response = producerTemplate.requestBody("direct:applicant01", request, Response.class);
        return response.toString();
    }

    @RequestMapping(value = "/call/appl02")
    public String callApplicant02(@RequestParam(value = "value") long value) {
        Request request = new Request("call-applicant02", new Date(), value);
        List<Response> responses = producerTemplate.requestBody("direct:applicant02", request, List.class);
        return responses.stream().map(Response::toString).collect(Collectors.joining("\n"));
    }

    @RequestMapping(value = "/rest/appl01")
    public ResponseEntity<Response> restApplicant01(@RequestBody Request request) {
        if (request.getName() == null)
            request.setName("rest-applicant01");
        request.setTs(new Date());

        Response response = producerTemplate.requestBody("direct:applicant01", request, Response.class);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/rest/appl02")
    public ResponseEntity<List<Response>> restApplicant02(@RequestBody Request request) {
        if (request.getName() == null)
            request.setName("rest-applicant02");
        request.setTs(new Date());

        List<Response> response = producerTemplate.requestBody("direct:applicant02", request, List.class);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}