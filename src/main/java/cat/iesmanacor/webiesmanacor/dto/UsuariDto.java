package cat.iesmanacor.webiesmanacor.dto;

import lombok.Data;

public @Data class UsuariDto {
    private long idUsuari;
    private String nom;
    private String foto;
    private String carrec1;
    private String carrec2;
    private String carrec3;
    private UsuariDto substitut;
    private Boolean visible;
    //Microservei CORE
    private CoreUsuariDto professor;
    private DepartamentDto departament;
    private String horariAtencioPares;
    private String tutoria;
}
