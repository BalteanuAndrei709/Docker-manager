package ai.openfabric.api.model;

import lombok.*;

import javax.persistence.*;

@ToString
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Port {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "of-uuid")
    @Column(name="id")
    public String id;

    @ManyToOne
    @JoinColumn(name="worker_id", nullable=false)
    private Worker worker;
    public String externPort;

    public String  internPort;

    public Port(String externPort, String internPort){
        this.externPort = externPort;
        this.internPort = internPort;
    }
}
