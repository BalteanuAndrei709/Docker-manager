package ai.openfabric.api.controller;

import ai.openfabric.api.Response.WorkerResponse;
import ai.openfabric.api.Service.WorkerService;
import ai.openfabric.api.model.Worker;
import ai.openfabric.api.model.WorkerStatus;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("${node.api.path}/worker")
public class WorkerController {

    private final WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @PostMapping(path = "/persist")
    public @ResponseBody String hello() {
        workerService.persistDb();
        return "Hey";
    }

    @GetMapping("/all")
    public ResponseEntity<Page<WorkerResponse>> listWorkers(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<WorkerResponse> responses = workerService.listWorkers(pageNumber, pageSize);
        return ResponseEntity.ok(responses);
    }
    @PostMapping("/start")
    public ResponseEntity<String> startWorker(@RequestBody String workerId){
        Worker worker = workerService.getWorkerById(workerId);
        if(Objects.equals(worker.getStatus(), WorkerStatus.running.toString())){
            return ResponseEntity.ok("Worker already working hard!");
        }
        return ResponseEntity.ok(workerService.startWorker(worker));
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopWorker(@RequestBody String workerId){
        Worker worker = workerService.getWorkerById(workerId);
        if(Objects.equals(worker.getStatus(), WorkerStatus.exited.toString())){
            return ResponseEntity.ok("Worker is resting already!");
        }
        return ResponseEntity.ok(workerService.stopWorker(worker));

    }

}
