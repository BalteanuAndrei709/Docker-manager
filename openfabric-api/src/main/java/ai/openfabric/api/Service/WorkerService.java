package ai.openfabric.api.Service;

import ai.openfabric.api.Response.WorkerResponse;
import ai.openfabric.api.config.DockerConfig;
import ai.openfabric.api.model.Port;
import ai.openfabric.api.model.Worker;
import ai.openfabric.api.repository.PortsRepository;
import ai.openfabric.api.repository.WorkerRepository;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerPort;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WorkerService {

    private final WorkerRepository workerRepository;

    private final PortsRepository portsRepository;


    private final DockerConfig dockerConfig = new DockerConfig();
    private final DockerClient dockerClient = dockerConfig.getDockerClient();

    public WorkerService(WorkerRepository workerRepository, PortsRepository portsRepository) {
        this.workerRepository = workerRepository;
        this.portsRepository = portsRepository;
    }

    public Page<WorkerResponse> listWorkers(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<Worker> workers = workerRepository.findAll(pageable);
        List<WorkerResponse> workerResponses = new ArrayList<>();

        for (Worker worker : workers.getContent()) {
            List<Port> ports = portsRepository.findAllByWorkerId(worker.getId());
            workerResponses.add(WorkerResponse.builder().worker(worker).ports(ports).build());
        }

        return new PageImpl<>(workerResponses, pageable, workers.getTotalElements());

    }


    public void persistDb() {

        List<Container> containerList = dockerClient.listContainersCmd().withShowAll(true).exec();
        for (Container container : containerList) {
            if (!workerRepository.existsByContainerId(container.getId())) {
                InspectContainerResponse inspectResponse = dockerConfig.getDockerClient().inspectContainerCmd(container.getId()).exec();

                String nameCorrected = Arrays.toString(container.getNames()).substring(2, Arrays.toString(container.getNames()).length() - 1);

                Map<Integer, Integer> map = extractPorts(container.getPorts());

                Worker worker = workerRepository.save(Worker.builder().containerId(container.getId()).name(nameCorrected).image(container.getImage()).status(inspectResponse.getState().getStatus()).build());
                for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                    portsRepository.save(
                            Port
                                    .builder()
                                    .internPort((entry.getValue() != null) ? entry.getValue().toString() : "")
                                    .externPort((entry.getKey() != null) ? entry.getKey().toString() : "")
                                    .worker(worker)
                                    .build());
                }
            }
        }
    }

    public Worker getWorkerById(String workerId) {
        return workerRepository.findById(workerId).orElse(null);
    }

    public String startWorker(Worker worker) {
        dockerClient.startContainerCmd(worker.getContainerId()).exec();
        if (isRunning(worker)) {
            actualizePort(worker);
            actualizeWorker(worker);
            return "Worker started working hard!";
        }
        return "Worker didn't want to work today :(!";
    }

    private void actualizePort(Worker worker) {
        Map<String, String> port = extractPorts(worker);
        for (Map.Entry<String, String> entry : port.entrySet()) {
            if (!portsRepository.existsByWorkerId(worker.getId())) {
                portsRepository.save(Port.builder().externPort(entry.getKey()).internPort(entry.getValue()).worker(worker).build());
            }
        }
    }

    private void actualizeWorker(Worker worker) {
        worker.setStatus("running");
        workerRepository.save(worker);
    }

    private Boolean isRunning(Worker worker) {
        InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(worker.getContainerId()).exec();
        return Boolean.TRUE.equals(containerInfo.getState().getRunning());

    }

    private Map<String, String> extractPorts(Worker worker) {
        // retrieve the container's network settings
        Ports ports = dockerClient.inspectContainerCmd(worker.getContainerId()).exec().getNetworkSettings().getPorts();

// create a map to store the port mappings
        Map<String, String> portMappings = new HashMap<>();

// iterate over the exposed ports
        for (Map.Entry<ExposedPort, Ports.Binding[]> entry : ports.getBindings().entrySet()) {

            // get the exposed port and bindings for this entry
            ExposedPort port = entry.getKey();
            Ports.Binding[] bindings = entry.getValue();

            // check if there are any bindings for this port
            if (bindings == null || bindings.length == 0) {
                // add an entry to the map with an empty string as the value
                portMappings.put(port.getPort() + "/" + port.getProtocol(), null);
            } else {
                // add an entry to the map with the first binding's host port as the value
                portMappings.put(port.getPort() + "/" + port.getProtocol(), bindings[0].getHostPortSpec());
            }
        }
        return portMappings;
    }

    private Map<Integer, Integer> extractPorts(ContainerPort[] ports) {
        Map<Integer, Integer> portsMap = new HashMap<>();
        for (ContainerPort port : ports) {
            portsMap.put(port.getPublicPort(), port.getPrivatePort());
        }
        return portsMap;
    }

    public String stopWorker(Worker worker) {
        try {
            dockerClient.stopContainerCmd(worker.getContainerId()).exec();
        } catch (NotModifiedException ex) {
            InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(worker.getContainerId()).exec();
            if (Boolean.FALSE.equals(containerInfo.getState().getRunning())) {
                // Container is already stopped
                // Handle this situation as appropriate
                worker.setStatus("exited");
                workerRepository.save(worker);
                portsRepository.delete(portsRepository.getByWorkerId(worker.getId()));
                return "Stopped";
            } else {
                return "Running";
            }
        }
        return "smt else";
    }
}
