package cz.dsw.distribguide.example09.rest;

import cz.dsw.distribguide.example09.entity.RequestA;
import cz.dsw.distribguide.example09.entity.RequestB;
import cz.dsw.distribguide.example09.entity.ResponseA;
import cz.dsw.distribguide.example09.entity.ResponseB;
import org.apache.camel.ProducerTemplate;
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

    private static final String HEADER_OBJECT_COMPRESSION = "ObjectCompression";

    @Autowired
    private ProducerTemplate producerTemplate;

    @RequestMapping(value = "/rest/appl01")
    public ResponseEntity<List<ResponseA>> restApplicant01(
            @RequestBody RequestA request,
            @RequestParam(value = "filter", required = false, defaultValue = "false") boolean filter) {

        if (request.getName() == null)
            request.setName("rest-applicant01");
        request.setTs(new Date());

        Map<String, Object> headers = new HashMap<>();
        String url = (!filter) ? "direct:applicant01" : "direct:applicant02";

        List<ResponseA> response = producerTemplate.requestBodyAndHeaders(url, request, headers, List.class);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/rest/appl02")
    public ResponseEntity<List<ResponseB>> restApplicant02(
            @RequestBody RequestB request,
            @RequestParam(value = "filter", required = false, defaultValue = "false") boolean filter) {

        if (request.getName() == null)
            request.setName("rest-applicant02");
        request.setTs(new Date());

        Map<String, Object> headers = new HashMap<>();
        String url = (!filter) ? "direct:applicant01" : "direct:applicant02";

        List<ResponseB> response = producerTemplate.requestBodyAndHeaders(url, request, headers, List.class);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}