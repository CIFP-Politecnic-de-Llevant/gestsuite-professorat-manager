package cat.politecnicllevant.professoratmanager.dto;

import lombok.Data;

public @Data class CursDto {
    private Long idcurs;
    private String gestibIdentificador;
    private String gestibNom;
    private String gsuiteUnitatOrganitzativa;
    private Boolean actiu;
}
