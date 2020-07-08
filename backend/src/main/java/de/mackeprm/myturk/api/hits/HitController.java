package de.mackeprm.myturk.api.hits;

import de.mackeprm.myturk.mturk.Endpoint;
import de.mackeprm.myturk.mturk.MturkApiService;
import de.mackeprm.myturk.services.HITService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.mturk.model.HIT;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@RestController
@RequestMapping("/api/v1/HITs")
public class HitController {
    private final HITService hitService;

    @Autowired
    public HitController(HITService hitService) {
        this.hitService = hitService;
    }

    //TODO include endpoints
    @GetMapping("/")
    public ResponseEntity<Map<Endpoint, List<String>>> findAll(@RequestParam(required = false) String token) {
        Map<Endpoint, List<String>> result = new HashMap<>();
        // new ResponseEntity<>(mturkApiService.listHitsByPage(10,token), HttpStatus.OK);

        result.put(Endpoint.sandbox, hitService.listAllHitIds(Endpoint.sandbox));
        result.put(Endpoint.production, hitService.listAllHitIds(Endpoint.production));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getHITbyId(@PathVariable String id) {
        throw new IllegalStateException("not implemented");
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<String> deleteHITbyId(@PathVariable String id) {
        throw new IllegalStateException("not implemented");
    }
}
