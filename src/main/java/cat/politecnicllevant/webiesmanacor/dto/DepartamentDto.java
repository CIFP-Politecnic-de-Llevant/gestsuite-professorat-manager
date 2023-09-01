package cat.politecnicllevant.webiesmanacor.dto;

import lombok.Data;

public @Data class DepartamentDto {
    private Long iddepartament;
    private String gestibIdentificador;
    private String gestibNom;
    private CoreUsuariDto capDepartament;
}
