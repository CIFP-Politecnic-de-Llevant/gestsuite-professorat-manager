package cat.iesmanacor.webiesmanacor.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "im_usuari")
public @Data class Usuari {
    @Id
    @Column(name = "idusuari")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuari;

    @Column(name = "foto", nullable = true, length = 2048)
    private String foto;

    @Column(name = "carrec1", nullable = true, length = 2048)
    private String carrec1;

    @Column(name = "carrec2", nullable = true, length = 2048)
    private String carrec2;

    @Column(name = "carrec3", nullable = true, length = 2048)
    private String carrec3;

    @OneToOne(optional = true)
    private Usuari substitut;

    //Microservei CORE
    @Column(name = "professor_idusuari", nullable = true)
    private Long professor;

    @Column(name = "departament_iddepartament", nullable = true)
    private Long departament;

    @Column(name = "horari_atencio_pares", nullable = true, length = 2048)
    private String horariAtencioPares;
}
