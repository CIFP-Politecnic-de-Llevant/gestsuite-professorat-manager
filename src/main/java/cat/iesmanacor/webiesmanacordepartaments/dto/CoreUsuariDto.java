package cat.iesmanacor.webiesmanacordepartaments.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

//@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public @Data class CoreUsuariDto implements Cloneable {
    private Long idusuari;
    private Boolean actiu;
    private Set<CoreRolDto> rols;
    private String gsuiteEmail;
    private Boolean gsuiteAdministrador;
    private String gsuitePersonalID;
    private Boolean gsuiteSuspes;
    private Boolean gsuiteEliminat;
    private String gsuiteUnitatOrganitzativa;
    private Boolean bloquejaGsuiteUnitatOrganitzativa;
    private String gsuiteGivenName;
    private String gsuiteFamilyName;
    private String gsuiteFullName;
    private String gestibCodi;
    private String gestibNom;
    private String gestibCognom1;
    private String gestibCognom2;
    private String gestibUsername;
    private String gestibExpedient;
    private String gestibGrup;
    private String gestibGrup2;
    private String gestibGrup3;
    private String gestibDepartament;
    private Boolean gestibProfessor;
    private Boolean gestibAlumne;
    public UsuariDto clone() throws CloneNotSupportedException {
        return (UsuariDto) super.clone();
    }
}
