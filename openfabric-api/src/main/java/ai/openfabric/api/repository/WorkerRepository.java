package ai.openfabric.api.repository;

import ai.openfabric.api.model.Worker;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkerRepository extends PagingAndSortingRepository<Worker, String> {
    Boolean existsByContainerId(String containerId);
}
