package ai.openfabric.api.Response;


import ai.openfabric.api.model.Port;
import ai.openfabric.api.model.Worker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkerResponse {

    public Worker worker;

    public List<Port> ports;


}
