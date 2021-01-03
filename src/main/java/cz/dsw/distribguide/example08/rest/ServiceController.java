package cz.dsw.distribguide.example08.rest;

import cz.dsw.distribguide.example08.entity.*;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ServiceController {

    @Autowired
    private ProducerTemplate producerTemplate;

    @RequestMapping(value = "/rest/appl01")
    public ResponseEntity<List<Response>> restApplicant01(@RequestBody Request request) {
        if (request.getName() == null)
            request.setName("rest-applicant01");
        request.setTs(new Date());

        Map<String, Object> headers = new HashMap<>();

        List<Response> response = producerTemplate.requestBodyAndHeaders("direct:applicant01", request, headers, List.class);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}