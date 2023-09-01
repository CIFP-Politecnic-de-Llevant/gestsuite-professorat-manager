package cat.iesmanacor.webiesmanacor.controller;

import cat.politecnicllevant.common.model.Notificacio;
import cat.politecnicllevant.common.model.NotificacioTipus;
import cat.politecnicllevant.common.service.UtilService;
import cat.iesmanacor.webiesmanacor.dto.*;
import cat.iesmanacor.webiesmanacor.restclient.CoreRestClient;
import cat.iesmanacor.webiesmanacor.service.UsuariService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UsuariController {

    @Autowired
    private CoreRestClient coreRestClient;

    @Autowired
    private UsuariService usuariService;

    @Autowired
    private Gson gson;


    @GetMapping("/usuari/llistat")
    public ResponseEntity<List<UsuariDto>> getUsuaris() throws Exception {
        ResponseEntity<List<SessioDto>> sessionsAtencioParesResponse = coreRestClient.getSessionsAtencioPares();
        List<SessioDto> sessionsAtencioPares = sessionsAtencioParesResponse.getBody();

        ResponseEntity<List<CoreUsuariDto>> usuarisResponse = coreRestClient.getUsuarisActius();
        List<CoreUsuariDto> usuaris = usuarisResponse.getBody();

        List<UsuariDto> result = new ArrayList<>();
        if (usuaris != null) {
            usuaris = usuaris.stream().filter(u -> u.getGestibProfessor() != null && u.getGestibProfessor() && u.getActiu()).collect(Collectors.toList());
            for (CoreUsuariDto usuariCore : usuaris) {
                UsuariDto usuari = usuariService.findByCoreIdUsuari(usuariCore.getIdusuari());

                if (usuari == null) {
                    usuari = new UsuariDto();
                }

                ResponseEntity<CoreUsuariDto> professorResponse = coreRestClient.getPublicProfile(usuariCore.getIdusuari().toString());
                CoreUsuariDto professor = professorResponse.getBody();

                if (professor != null) {
                    if(usuari.getNom()== null || usuari.getNom().isEmpty()) {
                        String nom = UtilService.capitalize(professor.getGestibNom());
                        String cognom1 = UtilService.capitalize(professor.getGestibCognom1());
                        String cognom2 = UtilService.capitalize(professor.getGestibCognom2());

                        usuari.setNom(cognom1 + " " + cognom2 + ", " + nom);
                    }

                    if(usuari.getVisible()==null){
                        usuari.setVisible(true);
                    }

                    usuari.setProfessor(professor);

                    //Sessions atenció pares
                    if (sessionsAtencioPares != null) {
                        List<SessioDto> sessionsProfessor = sessionsAtencioPares.stream().filter(s -> s.getGestibProfessor().equals(professor.getGestibCodi())).collect(Collectors.toList());
                        List<String> sessionsProfessorStr = new ArrayList<>();
                        for (SessioDto sessioDto : sessionsProfessor) {
                            LocalTime horaIniSessioPares = LocalTime.parse(sessioDto.getGestibHora());
                            LocalTime horaFiSessioPares = horaIniSessioPares.plusMinutes(Long.parseLong(sessioDto.getGestibDurada()));

                            String dia = "";
                            switch (sessioDto.getGestibDia()) {
                                case "1":
                                    dia = "Dilluns";
                                    break;
                                case "2":
                                    dia = "Dimarts";
                                    break;
                                case "3":
                                    dia = "Dimecres";
                                    break;
                                case "4":
                                    dia = "Dijous";
                                    break;
                                case "5":
                                    dia = "Divendres";
                                    break;
                                case "6":
                                    dia = "Dissabte";
                                    break;
                                case "7":
                                    dia = "Diumenge";
                                    break;
                            }

                            //String sessioStr = dia + " de " + horaIniSessioPares.format(DateTimeFormatter.ofPattern("HH:mm")) + " a " + horaFiSessioPares.format(DateTimeFormatter.ofPattern("HH:mm"));
                            String sessioStr = dia + " " + horaIniSessioPares.format(DateTimeFormatter.ofPattern("HH:mm"));

                            sessionsProfessorStr.add(sessioStr);
                        }
                        usuari.setHorariAtencioPares(String.join(", ", sessionsProfessorStr));
                    }

                    //Tutoria
                    ResponseEntity<List<GrupDto>> grupsTutorResponse = coreRestClient.getGrupsByTutor(professor.getIdusuari());
                    List<GrupDto> grupsTutor = grupsTutorResponse.getBody();
                    if (grupsTutor != null) {
                        List<String> grupsTutorStr = new ArrayList<>();
                        for (GrupDto grupDto : grupsTutor) {
                            ResponseEntity<CursDto> cursResponse = coreRestClient.getCursByCodiGestib(grupDto.getGestibCurs());
                            CursDto curs = cursResponse.getBody();

                            if(curs!=null) {
                                grupsTutorStr.add(curs.getGestibNom() + grupDto.getGestibNom());
                            }
                        }
                        usuari.setTutoria(String.join(", ", grupsTutorStr));
                    }

                }

                if (usuariCore.getGestibDepartament() != null && !usuariCore.getGestibDepartament().isEmpty()) {
                    ResponseEntity<DepartamentDto> departamentResponse = coreRestClient.getDepartamentByCodiGestib(usuariCore.getGestibDepartament());
                    DepartamentDto departament = departamentResponse.getBody();

                    if (departament != null) {
                        usuari.setDepartament(departament);
                    }
                }

                if(usuari.getSubstitut()!=null){
                    UsuariDto substitutDto = usuariService.findById(usuari.getSubstitut().getIdUsuari());

                    ResponseEntity<CoreUsuariDto> substitutResponse = coreRestClient.getProfile(substitutDto.getProfessor().getIdusuari().toString());
                    CoreUsuariDto substitut = substitutResponse.getBody();

                    if (substitut != null) {
                        substitutDto.setProfessor(substitut);

                        //Sessions atenció pares
                        if (sessionsAtencioPares != null) {
                            List<SessioDto> sessionsSubstitut = sessionsAtencioPares.stream().filter(s -> s.getGestibProfessor().equals(substitut.getGestibCodi())).collect(Collectors.toList());
                            List<String> sessionsSubstitutStr = new ArrayList<>();
                            for (SessioDto sessioDto : sessionsSubstitut) {
                                LocalTime horaIniSessioPares = LocalTime.parse(sessioDto.getGestibHora());
                                LocalTime horaFiSessioPares = horaIniSessioPares.plusMinutes(Long.parseLong(sessioDto.getGestibDurada()));

                                String dia = "";
                                switch (sessioDto.getGestibDia()) {
                                    case "1":
                                        dia = "Dilluns";
                                        break;
                                    case "2":
                                        dia = "Dimarts";
                                        break;
                                    case "3":
                                        dia = "Dimecres";
                                        break;
                                    case "4":
                                        dia = "Dijous";
                                        break;
                                    case "5":
                                        dia = "Divendres";
                                        break;
                                    case "6":
                                        dia = "Dissabte";
                                        break;
                                    case "7":
                                        dia = "Diumenge";
                                        break;
                                }

                                //String sessioStr = dia + " de " + horaIniSessioPares.format(DateTimeFormatter.ofPattern("HH:mm")) + " a " + horaFiSessioPares.format(DateTimeFormatter.ofPattern("HH:mm"));
                                String sessioStr = dia + " " + horaIniSessioPares.format(DateTimeFormatter.ofPattern("HH:mm"));

                                sessionsSubstitutStr.add(sessioStr);
                            }
                            substitutDto.setHorariAtencioPares(String.join(", ", sessionsSubstitutStr));
                        }

                        usuari.setSubstitut(substitutDto);
                    }
                }

                usuariService.save(usuari);

                result.add(usuari);
            }
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/usuari/getById/{id}")
    public ResponseEntity<UsuariDto> getCursById(@PathVariable("id") Long identificador) throws Exception {
        UsuariDto usuari = usuariService.findById(identificador);
        return new ResponseEntity<>(usuari, HttpStatus.OK);
    }

    @PostMapping("/usuari/desar")
    public ResponseEntity<Notificacio> desar(@RequestBody String json) throws Exception {
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        long idUsuari = jsonObject.get("id").getAsLong();
        String carrec1 = "";
        if (jsonObject.get("carrec1") != null && !jsonObject.get("carrec1").isJsonNull()) {
            carrec1 = jsonObject.get("carrec1").getAsString();
        }
        String carrec2 = "";
        if (jsonObject.get("carrec2") != null && !jsonObject.get("carrec2").isJsonNull()) {
            carrec2 = jsonObject.get("carrec2").getAsString();
        }
        String carrec3 = "";
        if (jsonObject.get("carrec3") != null && !jsonObject.get("carrec3").isJsonNull()) {
            carrec3 = jsonObject.get("carrec3").getAsString();
        }
        String foto = "";
        if (jsonObject.get("foto") != null && !jsonObject.get("foto").isJsonNull()) {
            foto = jsonObject.get("foto").getAsString();
        }

        UsuariDto substitut = null;
        if (jsonObject.get("substitut") != null && !jsonObject.get("substitut").isJsonNull()) {
            substitut = usuariService.findById(jsonObject.get("substitut").getAsJsonObject().get("id").getAsLong());
        }

        String nom = jsonObject.get("nom").getAsString();
        Boolean visible = jsonObject.get("visible").getAsBoolean();

        UsuariDto usuariOld = usuariService.findById(idUsuari);

        UsuariDto usuari = new UsuariDto();
        usuari.setIdUsuari(idUsuari);
        usuari.setNom(nom);
        usuari.setCarrec1(carrec1);
        usuari.setCarrec2(carrec2);
        usuari.setCarrec3(carrec3);
        usuari.setFoto(foto);
        usuari.setSubstitut(substitut);
        usuari.setVisible(visible);

        //Deixem el professor i el departament
        usuari.setProfessor(usuariOld.getProfessor());
        usuari.setDepartament(usuariOld.getDepartament());

        usuariService.save(usuari);

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Usuari desat correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

}