package cat.iesmanacor.webiesmanacor.controller;

import cat.iesmanacor.common.model.Notificacio;
import cat.iesmanacor.common.model.NotificacioTipus;
import cat.iesmanacor.webiesmanacor.dto.CoreUsuariDto;
import cat.iesmanacor.webiesmanacor.dto.DepartamentDto;
import cat.iesmanacor.webiesmanacor.dto.SessioDto;
import cat.iesmanacor.webiesmanacor.dto.UsuariDto;
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

                ResponseEntity<CoreUsuariDto> professorResponse = coreRestClient.getProfile(usuariCore.getIdusuari().toString());
                CoreUsuariDto professor = professorResponse.getBody();

                if (professor != null) {
                    usuari.setProfessor(professor);

                    //Sessions atenció pares
                    if (sessionsAtencioPares != null) {
                        List<SessioDto> sessionsProfessor = sessionsAtencioPares.stream().filter(s -> s.getGestibProfessor().equals(professor.getGestibCodi())).collect(Collectors.toList());
                        List<String> sessionsProfessorStr = new ArrayList<>();
                        for (SessioDto sessioDto : sessionsProfessor) {
                            LocalTime horaIniSessioPares = LocalTime.parse(sessioDto.getGestibHora());
                            LocalTime horaFiSessioPares = horaIniSessioPares.plusMinutes(Long.parseLong(sessioDto.getGestibDurada()));

                            String dia = "";
                            if (sessioDto.getGestibDia().equals("1")) {
                                dia = "Dilluns";
                            } else if (sessioDto.getGestibDia().equals("2")) {
                                dia = "Dimarts";
                            } else if (sessioDto.getGestibDia().equals("3")) {
                                dia = "Dimecres";
                            } else if (sessioDto.getGestibDia().equals("4")) {
                                dia = "Dijous";
                            } else if (sessioDto.getGestibDia().equals("5")) {
                                dia = "Divendres";
                            } else if (sessioDto.getGestibDia().equals("6")) {
                                dia = "Dissabte";
                            } else if (sessioDto.getGestibDia().equals("7")) {
                                dia = "Diumenge";
                            }

                            String sessioStr = dia + " de " + horaIniSessioPares.format(DateTimeFormatter.ofPattern("HH:mm")) + " a " + horaFiSessioPares.format(DateTimeFormatter.ofPattern("HH:mm"));

                            sessionsProfessorStr.add(sessioStr);
                        }
                        usuari.setHorariAtencioPares(String.join(", ", sessionsProfessorStr));
                    }
                }

                if (usuariCore.getGestibDepartament() != null && !usuariCore.getGestibDepartament().isEmpty()) {
                    ResponseEntity<DepartamentDto> departamentResponse = coreRestClient.getDepartamentByCodiGestib(usuariCore.getGestibDepartament());
                    DepartamentDto departament = departamentResponse.getBody();

                    if (departament != null) {
                        usuari.setDepartament(departament);
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

        Long idUsuari = jsonObject.get("id").getAsLong();
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

        UsuariDto usuariOld = usuariService.findById(idUsuari);

        UsuariDto usuari = new UsuariDto();
        usuari.setIdUsuari(idUsuari);
        usuari.setCarrec1(carrec1);
        usuari.setCarrec2(carrec2);
        usuari.setCarrec3(carrec3);
        usuari.setFoto(foto);
        usuari.setSubstitut(substitut);

        //Deixem el professor i el departament
        usuari.setProfessor(usuariOld.getProfessor());
        usuari.setDepartament(usuariOld.getDepartament());

        usuariService.save(usuari);

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Usuari desat correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @GetMapping(value = "/public/loadDepartament/{id}/script.js", produces = "text/javascript")
    public String generarScript(@PathVariable("id") Long identificador) throws Exception {

        ResponseEntity<List<CoreUsuariDto>> usuarisResponse = coreRestClient.getUsuarisByDepartament(identificador);
        List<CoreUsuariDto> usuaris = usuarisResponse.getBody();

        int idx = 0;
        String script = "";

        script += "<style>";

        script += ".professors{";
        script += "    display: flex; !important;";
        script += "    flex-wrap: wrap;";
        script += "    justify-content: center;";
        script += "    gap: 20px;";
        script += "}";

        script += ".professor{";
        script += "     background: #E7E7E7;";
        script += "     padding: 10px;";
        script += "     width: 255px;";
        script += "}";

        script += ".professor .foto{";
        script += "     width: 235px;";
        script += "     height: 235px;";
        script += "}";

        script += ".professor .foto img{";
        script += "     object-fit: cover;";
        script += "}";

        script += ".professor h3{";
        script += "     font-family: \"Roboto\", Sans-serif;";
        script += "     font-size: 0.9em;";
        script += "     font-weight: 400;";
        script += "     text-align: center;";
        script += "}";

        script += "</style>";

        script += "<div class=\"professors elementor-container elementor-column-gap-default\">";
        if (usuaris != null && usuaris.size() > 0) {
            for (CoreUsuariDto usuariCore : usuaris) {
                UsuariDto usuari = usuariService.findByCoreIdUsuari(usuariCore.getIdusuari());
                if (usuari != null) {
                    script += "<div class=\"professor\">";

                    //Foto
                    if (usuari.getFoto() != null) {
                        script += "<div class=\"foto\">";
                        script += "<figure><img src=\"https://www.iesmanacor.cat/wp-content/uploads/FOTOS/" + usuari.getFoto() + "\" alt=\"\"></figure>";
                        script += "</div>";
                    }

                    //Nom i cognoms
                    script += "<h3>"+usuari.getProfessor().getGestibCognom1() + " " + usuari.getProfessor().getGestibCognom2()+", "+usuari.getProfessor().getGestibNom()+"</h3>";

                    //Càrrecs
                    script += "<div class=\"carrecs\">";
                    if (usuari.getCarrec1() != null && !usuari.getCarrec1().isEmpty()) {
                        script += usuari.getCarrec1();
                    }
                    if (usuari.getCarrec2() != null && !usuari.getCarrec2().isEmpty()) {
                        script += "<br>" + usuari.getCarrec2();
                    }
                    if (usuari.getCarrec3() != null && !usuari.getCarrec3().isEmpty()) {
                        script += "<br>" + usuari.getCarrec3();
                    }
                    script += "</div>";

                    //Horari tutoria
                    script += "<p>"+usuari.getHorariAtencioPares()+"</p>";

                    if (usuari.getProfessor() != null && usuari.getProfessor().getGsuiteEmail() != null) {
                        script += "<a href=\"mailto:"+usuari.getProfessor().getGsuiteEmail()+"\">";
                        script += "<span class=\"email\">" + usuari.getProfessor().getGsuiteEmail() + "</span>";
                        script += "</a>";
                    }


                    script += "</div>"; //class professor
                }
            }
        }
        script += "</div>"; //professors

        String result = " document.querySelector(\".elementor-widget-container .elementor-element .elementor-widget-text-editor\").innerHTML=''; ";
        result += " var resultScript = document.querySelector(\".elementor-widget-container .elementor-element .elementor-widget-text-editor\"); ";
        result += " if(resultScript){ resultScript.innerHTML = `" + script + "`; } ";

        return result;
    }


    public String generarScriptOld(@PathVariable("id") Long identificador) throws Exception {

        ResponseEntity<List<CoreUsuariDto>> usuarisResponse = coreRestClient.getUsuarisByDepartament(identificador);
        List<CoreUsuariDto> usuaris = usuarisResponse.getBody();

        int idx = 0;
        String script = "";

        if (usuaris != null && usuaris.size() > 0) {
            for (CoreUsuariDto usuariCore : usuaris) {
                UsuariDto usuari = usuariService.findByCoreIdUsuari(usuariCore.getIdusuari());
                if (usuari != null) {
                    if (idx % 4 == 0) {
                        //Secció de 4 professors
                        script += "<section class=\"elementor-section elementor-inner-section elementor-element elementor-element-3c94881e elementor-section-boxed elementor-section-height-default jet-parallax-section\" data-id=\"3c94881e\" data-element_type=\"section\" data-settings=\"{&quot;jet_parallax_layout_list&quot;:[{&quot;jet_parallax_layout_image&quot;:{&quot;url&quot;:&quot;&quot;,&quot;id&quot;:&quot;&quot;},&quot;_id&quot;:&quot;621665c&quot;,&quot;jet_parallax_layout_image_tablet&quot;:{&quot;url&quot;:&quot;&quot;,&quot;id&quot;:&quot;&quot;},&quot;jet_parallax_layout_image_mobile&quot;:{&quot;url&quot;:&quot;&quot;,&quot;id&quot;:&quot;&quot;},&quot;jet_parallax_layout_speed&quot;:{&quot;unit&quot;:&quot;%&quot;,&quot;size&quot;:50,&quot;sizes&quot;:[]},&quot;jet_parallax_layout_type&quot;:&quot;scroll&quot;,&quot;jet_parallax_layout_direction&quot;:null,&quot;jet_parallax_layout_fx_direction&quot;:null,&quot;jet_parallax_layout_z_index&quot;:&quot;&quot;,&quot;jet_parallax_layout_bg_x&quot;:50,&quot;jet_parallax_layout_bg_x_tablet&quot;:&quot;&quot;,&quot;jet_parallax_layout_bg_x_mobile&quot;:&quot;&quot;,&quot;jet_parallax_layout_bg_y&quot;:50,&quot;jet_parallax_layout_bg_y_tablet&quot;:&quot;&quot;,&quot;jet_parallax_layout_bg_y_mobile&quot;:&quot;&quot;,&quot;jet_parallax_layout_bg_size&quot;:&quot;auto&quot;,&quot;jet_parallax_layout_bg_size_tablet&quot;:&quot;&quot;,&quot;jet_parallax_layout_bg_size_mobile&quot;:&quot;&quot;,&quot;jet_parallax_layout_animation_prop&quot;:&quot;transform&quot;,&quot;jet_parallax_layout_on&quot;:[&quot;desktop&quot;,&quot;tablet&quot;]}]}\">";

                        script += "<div class=\"jet-parallax-section__layout elementor-repeater-item-621665c jet-parallax-section__scroll-layout is-mac\">" +
                                "<div class=\"jet-parallax-section__image\" style=\"background-position: 50% 50%; background-image: url(&quot;&quot;); transform: translateY(119.4px);\">" +
                                "</div>" +
                                "</div>";
                    }
                    //Professor
                    script += "<div class=\"elementor-container elementor-column-gap-default\">"; //Contenidor 1

                    //4 professors
                    script += "<div class=\"elementor-column elementor-col-25 elementor-inner-column elementor-element elementor-element-7aa9b17\" data-id=\"7aa9b17\" data-element_type=\"column\">";
                    script += "<div class=\"elementor-widget-wrap elementor-element-populated\">";
                    script += "<div class=\"elementor-element elementor-element-22c8157 elementor-widget elementor-widget-jet-team-member\" data-id=\"22c8157\" data-element_type=\"widget\" data-widget_type=\"jet-team-member.default\">";
                    script += "<div class=\"elementor-widget-container\">";
                    script += "<div class=\"elementor-jet-team-member jet-elements\">";
                    script += "<div class=\"jet-team-member\">";
                    script += "<div class=\"jet-team-member__inner\">";
                    script += "<div class=\"jet-team-member__image\">";
                    if (usuari.getFoto() != null) {
                        script += "<div class=\"jet-team-member__cover\"></div>";
                        script += "<figure class=\"jet-team-member__figure\"><img class=\"jet-team-member__img-tag\" src=\"https://www.iesmanacor.cat/wp-content/uploads/FOTOS/" + usuari.getFoto() + "\" alt=\"\"></figure>";
                        script += "</div>";
                    }
                    script += "<div class=\"jet-team-member__content\">";
                    script += "<h3 class=\"jet-team-member__name\"><span class=\"jet-team-member__name-first\">"+usuari.getProfessor().getGestibCognom1() + " " + usuari.getProfessor().getGestibCognom2()+",</span><span class=\"jet-team-member__name-last\"> "+usuari.getProfessor().getGestibNom()+"</span></h3>";
                    script += "<div class=\"jet-team-member__position\">";
                    if (usuari.getCarrec1() != null && !usuari.getCarrec1().isEmpty()) {
                        script += usuari.getCarrec1();
                    }
                    if (usuari.getCarrec2() != null && !usuari.getCarrec2().isEmpty()) {
                        script += "<br>" + usuari.getCarrec2();
                    }
                    if (usuari.getCarrec3() != null && !usuari.getCarrec3().isEmpty()) {
                        script += "<br>" + usuari.getCarrec3();
                    }
                    script += "</div>";
                    script += "<p class=\"jet-team-member__desc\">"+usuari.getHorariAtencioPares()+"</p>";

                    if (usuari.getProfessor() != null && usuari.getProfessor().getGsuiteEmail() != null) {
                        script += "<div class=\"jet-team-member__socials\">";
                        script += "<div class=\"jet-team-member__socials-item\"><a href=\"#\"><span class=\"jet-team-member__socials-label\">" + usuari.getProfessor().getGsuiteEmail() + "</span></a></div>";
                        script += "</div>";
                    }
                    script += "</div>";
                    script += "</div>";
                    script += "</div>";
                    script += "</div>";
                    script += "</div>";
                    script += "</div>";
                    script += "</div>";
                    script += "</div>";


                    script += "</div>"; //Contenidor 1

                    if (idx % 4 == 3) {
                        script += "</section>";
                    }
                    idx++;
                }
            }
        }

        String result = " document.querySelector(\".elementor-widget-container .elementor-element .elementor-widget-text-editor\").innerHTML=''; ";
        result += " var result = document.querySelector(\".elementor-widget-container .elementor-element .elementor-widget-text-editor\"); ";
        result += " if(result){ result.innerHTML = `" + script + "`; } ";

        return result;
    }

}