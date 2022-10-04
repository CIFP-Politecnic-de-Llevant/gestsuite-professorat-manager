package cat.iesmanacor.webiesmanacordepartaments.dto;

import lombok.Data;

public @Data class UsuariDto {
    private long idUsuari;
    private String foto;
    private String carrec1;
    private String carrec2;
    private String carrec3;
    //Microservei CORE
    private CoreUsuariDto professor;
    private DepartamentDto departament;
}
