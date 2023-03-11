package ai.openfabric.api.repository;


import ai.openfabric.api.model.Port;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PortsRepository extends PagingAndSortingRepository<Port, String> {
    List<Port> findAllByWorkerId(String workerId);

    void deleteByWorkerId(String workerId);

    Port getByWorkerId(String workerId);

    Boolean existsByWorkerId(String workerId);

}
