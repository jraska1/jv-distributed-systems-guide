package cz.dsw.distribguide.example07.rest;

import cz.dsw.distribguide.example07.entity.Request;
import cz.dsw.distribguide.example07.entity.Response;
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
    public ResponseEntity<List<Response>> restApplicant01(
            @RequestBody Request request,
            @RequestParam(value = "compression", required = false) String objectCompression) {
        if (request.getName() == null)
            request.setName("rest-applicant01");
        request.setTs(new Date());

        Map<String, Object> headers = new HashMap<>();
        if (objectCompression != null && objectCompression.length() > 0)
            headers.put(HEADER_OBJECT_COMPRESSION, objectCompression.toLowerCase());

        List<Response> response = producerTemplate.requestBodyAndHeaders("direct:applicant01", request, headers, List.class);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}