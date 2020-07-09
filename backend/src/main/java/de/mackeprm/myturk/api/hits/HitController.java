package de.mackeprm.myturk.api.hits;

import de.mackeprm.myturk.model.LocalHIT;
import de.mackeprm.myturk.mturk.Endpoint;
import de.mackeprm.myturk.services.HITService;
import de.mackeprm.myturk.sync.MturkSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/HITs")
public class HitController {
    private final HITService hitService;
    private final MturkSyncService mturkSyncService;

    @Autowired
    public HitController(HITService hitService, MturkSyncService mturkSyncService) {
        this.hitService = hitService;
        this.mturkSyncService = mturkSyncService;
    }

    @GetMapping("/")
    public ResponseEntity<Map<Endpoint, List<LocalHIT>>> findAll() {
        Map<Endpoint, List<LocalHIT>> result = new HashMap<>();
        // new ResponseEntity<>(mturkApiService.listHitsByPage(10,token), HttpStatus.OK);

        result.put(Endpoint.sandbox, hitService.listAllHitIds(Endpoint.sandbox));
        result.put(Endpoint.production, hitService.listAllHitIds(Endpoint.production));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{id}/")
    public ResponseEntity<LocalHIT> getHITbyId(@PathVariable String id) {
        Optional<LocalHIT> hitById = hitService.getHitById(id);
        if (hitById.isPresent()) {
            return new ResponseEntity<>(mturkSyncService.syncHit(hitById.get()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<String> deleteHITbyId(@PathVariable String id) {
        throw new IllegalStateException("not implemented");
    }
}
