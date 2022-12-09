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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class DepartamentController {

    @Autowired
    private CoreRestClient coreRestClient;

    @Autowired
    private UsuariService usuariService;

    @Autowired
    private Gson gson;


    @PostMapping(value = "/departament/generarScript")
    public ResponseEntity<Notificacio> generarScript(@RequestBody String json) throws Exception {
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        Long identificador = jsonObject.get("id").getAsLong();

        String script = this.getScriptByDepartamentID(identificador);
        this.generateScript(script,identificador);

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Script generat correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @PostMapping(value = "/departament/recuperarBackupScript")
    public ResponseEntity<Notificacio> recuperarBackup(@RequestBody String json) throws Exception {
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        Long identificador = jsonObject.get("id").getAsLong();

        this.recoverBackup(identificador);

        Notificacio notificacio = new Notificacio();
        notificacio.setNotifyMessage("Script recuperat correctament");
        notificacio.setNotifyType(NotificacioTipus.SUCCESS);
        return new ResponseEntity<>(notificacio, HttpStatus.OK);
    }

    @GetMapping(value = "/public/loadDepartament/{id}/script.js", produces = "text/javascript")
    public String loadScript(@PathVariable("id") Long identificador) throws Exception {
        final String FILE_NAME = "/tmp/departament_"+identificador+".txt";

        Path path = Paths.get(FILE_NAME);

        String content;
        if (Files.exists(path)) {
            System.out.println("File already exists");
            Files.newBufferedReader(path, StandardCharsets.UTF_8);
            content = Files.readString(path);

        } else {
            content = this.getScriptByDepartamentID(identificador);
            this.generateScript(content,identificador);
        }

        return content;
    }

    private void generateScript(String script, Long identificador) throws Exception {
        final String FILE_NAME = "/tmp/departament_"+identificador+".txt";

        //Backup
        Path path = Paths.get(FILE_NAME);
        if (Files.exists(path)) {
            Path pathbackup = Paths.get(FILE_NAME + "-backup");
            Files.copy(path, pathbackup, StandardCopyOption.REPLACE_EXISTING);
        }

        FileWriter myWriter = new FileWriter(FILE_NAME);
        myWriter.write(script);
        myWriter.close();
        /*FileOutputStream fos = new FileOutputStream(FILE_NAME);
        DataOutputStream outStream = new DataOutputStream(new BufferedOutputStream(fos));
        outStream.writeUTF(script);
        outStream.close();*/
    }

    private void recoverBackup(Long identificador) throws IOException {
        final String FILE_NAME = "/tmp/departament_"+identificador+".txt";

        //Backup
        Path path = Paths.get(FILE_NAME);
        Path pathbackup = Paths.get(FILE_NAME + "-backup");
        if (Files.exists(pathbackup)) {
            Files.copy(pathbackup, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }


    private String getScriptByDepartamentID(Long identificador) throws Exception {
        ResponseEntity<List<CoreUsuariDto>> usuarisResponse = coreRestClient.getUsuarisByDepartament(identificador);
        List<CoreUsuariDto> usuaris = usuarisResponse.getBody().stream().filter(CoreUsuariDto::getActiu).collect(Collectors.toList());

        String script = "";

        script += "<style>";

        script += "@import url('https://fonts.googleapis.com/css2?family=Montserrat+Alternates&display=swap');";

        script += ".professors{";
        script += "    display: flex !important;";
        script += "    flex-wrap: wrap !important;";
        script += "    justify-content: center;";
        script += "    gap: 20px;";
        script += "}";

        script += ".professor{";
        script += "     background: #E7E7E7;";
        script += "     padding: 10px;";
        script += "     width: 255px;";
        script += "     display: flex;";
        script += "     flex-flow: column;";
        script += "     align-items: center;";
        script += "     justify-content: start;";
        script += "}";

        script += ".professor .informacio{";
        script += "     display: flex;";
        script += "     flex-flow: column;";
        script += "     align-items: center;";
        script += "     justify-content: space-between;";
        script += "     height: 100%;";
        script += "}";

        script += ".professor .foto{";
        script += "     width: 235px;";
        script += "     height: 235px;";
        script += "     position: relative;";
        script += "}";

        script += ".professor .foto-titular{";
        script += "     width: 80px;";
        script += "     height: 80px;";
        script += "     filter: grayscale(1);";
        script += "     position: absolute;";
        script += "     bottom: 3px;";
        script += "     right: 3px;";
        script += "     margin: 0px;";
        script += "     padding: 0px;";
        script += "}";

        script += ".professor .foto-titular img{";
        script += "     border-radius: 2px;";
        script += "}";

        script += ".professor .foto img{";
        script += "     object-fit: cover;";
        script += "}";

        script += ".professor h3{";
        script += "     font-family: \"Roboto\", Sans-serif;";
        script += "     font-size: 0.9em;";
        script += "     font-weight: 400;";
        script += "     text-align: center;";
        script += "     margin: 10px 0;";
        script += "}";

        script += ".professor .carrecs{";
        script += "     color: #EE863A;";
        script += "     font-family: \"Roboto\", Sans-serif;";
        script += "     font-size: 14px;";
        script += "     font-weight: 400;";
        script += "     text-align: center;";
        script += "}";

        script += ".professor .horaritutoria{";
        script += "     color: #54595f;";
        script += "     font-family: \"Montserrat Alternates\", Sans-serif;";
        script += "     font-size: 13px;";
        script += "     font-weight: normal;";
        script += "     text-align: center;";
        script += "}";

        script += ".professor .nomsubstitut{";
        script += "     color: #54595f;";
        script += "     font-family: \"Montserrat\", Sans-serif;";
        script += "     font-size: 13px;";
        script += "     font-weight: normal;";
        script += "     text-align: center;";
        script += "}";


        script += ".professor .email{";
        script += "     color: #54595F;";
        script += "     font-family: \"Roboto\", Sans-serif;";
        script += "     font-size: 13px;";
        script += "     font-weight: 400;";
        script += "     text-align: center;";
        script += "}";


        script += "</style>";

        script += "<div class=\"professors elementor-container elementor-column-gap-default\">";
        if (usuaris != null && usuaris.size() > 0) {

            Collections.sort(usuaris);

            for (CoreUsuariDto usuariCore : usuaris) {
                UsuariDto usuari = usuariService.findByCoreIdUsuari(usuariCore.getIdusuari());
                boolean isSubstitut = usuariService.usuariIsSubstitut(usuari.getIdUsuari());
                if (usuari != null && !isSubstitut) {

                    UsuariDto usuariSubstitut = null;
                    if(usuari.getSubstitut() != null){
                        usuariSubstitut = usuariService.findById(usuari.getSubstitut().getIdUsuari());
                    }

                    script += "<div class=\"professor\">";

                    //Foto
                    if (usuari.getFoto() != null) {
                        if(usuariSubstitut==null) {
                            script += "<div class=\"foto\">";
                            script += "<figure><img src=\"https://www.iesmanacor.cat/wp-content/uploads/FOTOS/" + usuari.getFoto() + "\" alt=\"\"></figure>";
                            script += "</div>";
                        } else {
                            script += "<div class=\"foto\">";
                            script += "<figure class=\"foto-substitut\"><img src=\"https://www.iesmanacor.cat/wp-content/uploads/FOTOS/" + usuariSubstitut.getFoto() + "\" alt=\"\"></figure>";
                            script += "<figure class=\"foto-titular\"><img src=\"https://www.iesmanacor.cat/wp-content/uploads/FOTOS/" + usuari.getFoto() + "\" alt=\"\"></figure>";
                            script += "</div>";
                        }
                    }

                    script += "<div class=\"informacio\">";

                    //Nom i cognoms
                    if(usuariSubstitut==null) {
                        script += "<h3>" + usuari.getNom() + "</h3>";
                    } else {
                        script += "<h3>" + usuariSubstitut.getNom() + "</h3>";
                        script += "<p class=\"nomsubstitut\">(Substitueix a "+usuari.getNom()+")</p>";
                    }
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
                    script += "<p class=\"horaritutoria\">"+usuari.getHorariAtencioPares()+"</p>";

                    if(usuariSubstitut==null) {
                        if (usuari.getProfessor().getGsuiteEmail() != null) {
                            script += "<p class=\"email\"><a href=\"mailto:" + usuari.getProfessor().getGsuiteEmail() + "\">";
                            script += usuari.getProfessor().getGsuiteEmail();
                            script += "</a></p>";
                        }
                    } else {
                        if (usuariSubstitut.getProfessor().getGsuiteEmail() != null) {
                            script += "<p class=\"email\"><a href=\"mailto:" + usuariSubstitut.getProfessor().getGsuiteEmail() + "\">";
                            script += usuariSubstitut.getProfessor().getGsuiteEmail();
                            script += "</a></p>";
                        }
                    }

                    script += "</div>"; //class informació

                    script += "</div>"; //class professor
                }
            }
        }
        script += "</div>"; //professors

        String selector = "section .elementor-container .elementor-column .elementor-element.elementor-widget.elementor-widget-text-editor";

        String result = " document.querySelector(\""+selector+"\").innerHTML=''; ";
        result += " var resultScript = document.querySelector(\""+selector+"\"); ";
        result += " if(resultScript){ resultScript.innerHTML = `" + script + "`; } ";

        return result;
    }

}