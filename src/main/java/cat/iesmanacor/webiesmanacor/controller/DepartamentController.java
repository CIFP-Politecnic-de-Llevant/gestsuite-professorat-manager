package cat.iesmanacor.webiesmanacor.controller;

import cat.iesmanacor.common.model.Notificacio;
import cat.iesmanacor.common.model.NotificacioTipus;
import cat.iesmanacor.webiesmanacor.dto.CoreUsuariDto;
import cat.iesmanacor.webiesmanacor.dto.UsuariDto;
import cat.iesmanacor.webiesmanacor.restclient.CoreRestClient;
import cat.iesmanacor.webiesmanacor.service.UsuariService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class DepartamentController {

    @Autowired
    private CoreRestClient coreRestClient;

    @Autowired
    private UsuariService usuariService;

    @Value("${web.script.centre}")
    private String scriptCentre;

    @Autowired
    private Gson gson;


    @PostMapping(value = "/departament/generarScript")
    public ResponseEntity<Notificacio> generarScript(@RequestBody String json) throws Exception {
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        Long identificador = jsonObject.get("id").getAsLong();

        String script = "";
        if(scriptCentre.equals("iesmanacor")) {
            script = this.getScriptByDepartamentIDIESManacor(identificador);
        } else if(scriptCentre.equals("politecnicllevant")){
            script = this.getScriptByDepartamentIDCIFPPolitecnicLlevant(identificador);
        }
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
            if(scriptCentre.equals("iesmanacor")) {
                content = this.getScriptByDepartamentIDIESManacor(identificador);
            } else if(scriptCentre.equals("politecnicllevant")){
                content = this.getScriptByDepartamentIDCIFPPolitecnicLlevant(identificador);
            } else {
                content = "";
            }
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


    private String getScriptByDepartamentIDIESManacor(Long identificador) throws Exception {
        ResponseEntity<List<CoreUsuariDto>> usuarisResponse = coreRestClient.getUsuarisByDepartament(identificador);
        List<CoreUsuariDto> usuaris = usuarisResponse.getBody().stream().filter(CoreUsuariDto::getActiu).collect(Collectors.toList());

        StringBuilder script = new StringBuilder();

        script.append("<style>");

        script.append("@import url('https://fonts.googleapis.com/css2?family=Montserrat+Alternates&display=swap');");

        script.append(".professors{");
        script.append("    display: flex !important;");
        script.append("    flex-wrap: wrap !important;");
        script.append("    justify-content: center;");
        script.append("    gap: 20px;");
        script.append("}");

        script.append(".professor{");
        script.append("     background: #E7E7E7;");
        script.append("     padding: 10px;");
        script.append("     width: 255px;");
        script.append("     display: flex;");
        script.append("     flex-flow: column;");
        script.append("     align-items: center;");
        script.append("     justify-content: start;");
        script.append("}");

        script.append(".professor .informacio{");
        script.append("     display: flex;");
        script.append("     flex-flow: column;");
        script.append("     align-items: center;");
        script.append("     justify-content: space-between;");
        script.append("     height: 100%;");
        script.append("}");

        script.append(".professor .foto{");
        script.append("     width: 235px;");
        script.append("     height: 235px;");
        script.append("     position: relative;");
        script.append("}");

        script.append(".professor .foto-titular{");
        script.append("     width: 80px;");
        script.append("     height: 80px;");
        script.append("     filter: grayscale(1);");
        script.append("     position: absolute;");
        script.append("     bottom: 3px;");
        script.append("     right: 3px;");
        script.append("     margin: 0px;");
        script.append("     padding: 0px;");
        script.append("}");

        script.append(".professor .foto-titular img{");
        script.append("     border-radius: 2px;");
        script.append("}");

        script.append(".professor .foto img{");
        script.append("     object-fit: cover;");
        script.append("}");

        script.append(".professor h3{");
        script.append("     font-family: \"Roboto\", Sans-serif;");
        script.append("     font-size: 0.9em;");
        script.append("     font-weight: 400;");
        script.append("     text-align: center;");
        script.append("     margin: 10px 0;");
        script.append("}");

        script.append(".professor .carrecs{");
        script.append("     color: #EE863A;");
        script.append("     font-family: \"Roboto\", Sans-serif;");
        script.append("     font-size: 14px;");
        script.append("     font-weight: 400;");
        script.append("     text-align: center;");
        script.append("}");

        script.append(".professor .horaritutoria{");
        script.append("     color: #54595f;");
        script.append("     font-family: \"Montserrat Alternates\", Sans-serif;");
        script.append("     font-size: 13px;");
        script.append("     font-weight: normal;");
        script.append("     text-align: center;");
        script.append("}");

        script.append(".professor .nomsubstitut{");
        script.append("     color: #54595f;");
        script.append("     font-family: \"Montserrat\", Sans-serif;");
        script.append("     font-size: 13px;");
        script.append("     font-weight: normal;");
        script.append("     text-align: center;");
        script.append("}");


        script.append(".professor .email{");
        script.append("     color: #54595F;");
        script.append("     font-family: \"Roboto\", Sans-serif;");
        script.append("     font-size: 13px;");
        script.append("     font-weight: 400;");
        script.append("     text-align: center;");
        script.append("}");


        script.append("</style>");

        script.append("<div class=\"professors elementor-container elementor-column-gap-default\">");
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

                    script.append("<div class=\"professor\">");

                    //Foto
                    if (usuari.getFoto() != null) {
                        if(usuariSubstitut==null) {
                            script.append("<div class=\"foto\">");
                            script.append("<figure><img src=\"https://www.iesmanacor.cat/wp-content/uploads/FOTOS/").append(usuari.getFoto()).append("\" alt=\"\"></figure>");
                            script.append("</div>");
                        } else {
                            script.append("<div class=\"foto\">");
                            script.append("<figure class=\"foto-substitut\"><img src=\"https://www.iesmanacor.cat/wp-content/uploads/FOTOS/").append(usuariSubstitut.getFoto()).append("\" alt=\"\"></figure>");
                            script.append("<figure class=\"foto-titular\"><img src=\"https://www.iesmanacor.cat/wp-content/uploads/FOTOS/").append(usuari.getFoto()).append("\" alt=\"\"></figure>");
                            script.append("</div>");
                        }
                    }

                    script.append("<div class=\"informacio\">");

                    //Nom i cognoms
                    if(usuariSubstitut==null) {
                        script.append("<h3>").append(usuari.getNom()).append("</h3>");
                    } else {
                        script.append("<h3>").append(usuariSubstitut.getNom()).append("</h3>");
                        script.append("<p class=\"nomsubstitut\">(Substitueix a ").append(usuari.getNom()).append(")</p>");
                    }
                    //Càrrecs
                    script.append("<div class=\"carrecs\">");
                    if (usuari.getTutoria() != null && !usuari.getTutoria().isEmpty()) {
                        script.append("Tutoria ").append(usuari.getTutoria());
                    }
                    if (usuari.getCarrec1() != null && !usuari.getCarrec1().isEmpty()) {
                        if (usuari.getTutoria() != null && !usuari.getTutoria().isEmpty()) {
                            script.append("<br>");
                        }
                        script.append(usuari.getCarrec1());
                    }
                    if (usuari.getCarrec2() != null && !usuari.getCarrec2().isEmpty()) {
                        script.append("<br>").append(usuari.getCarrec2());
                    }
                    if (usuari.getCarrec3() != null && !usuari.getCarrec3().isEmpty()) {
                        script.append("<br>").append(usuari.getCarrec3());
                    }
                    script.append("</div>");

                    //Horari tutoria
                    script.append("<p class=\"horaritutoria\">").append(usuari.getHorariAtencioPares()).append("</p>");

                    if(usuariSubstitut==null) {
                        if (usuari.getProfessor().getGsuiteEmail() != null) {
                            script.append("<p class=\"email\"><a href=\"mailto:").append(usuari.getProfessor().getGsuiteEmail()).append("\">");
                            script.append(usuari.getProfessor().getGsuiteEmail());
                            script.append("</a></p>");
                        }
                    } else {
                        if (usuariSubstitut.getProfessor().getGsuiteEmail() != null) {
                            script.append("<p class=\"email\"><a href=\"mailto:").append(usuariSubstitut.getProfessor().getGsuiteEmail()).append("\">");
                            script.append(usuariSubstitut.getProfessor().getGsuiteEmail());
                            script.append("</a></p>");
                        }
                    }

                    script.append("</div>"); //class informació

                    script.append("</div>"); //class professor
                }
            }
        }
        script.append("</div>"); //professors

        String selector = "section .elementor-container .elementor-column .elementor-element.elementor-widget.elementor-widget-text-editor";

        String result = " document.querySelector(\""+selector+"\").innerHTML=''; ";
        result += " var resultScript = document.querySelector(\""+selector+"\"); ";
        result += " if(resultScript){ resultScript.innerHTML = `" + script + "`; } ";

        return result;
    }

    private String getScriptByDepartamentIDCIFPPolitecnicLlevant(Long identificador) throws Exception {
        ResponseEntity<List<CoreUsuariDto>> usuarisResponse = coreRestClient.getUsuarisByDepartament(identificador);
        List<CoreUsuariDto> usuaris = usuarisResponse.getBody().stream().filter(CoreUsuariDto::getActiu).collect(Collectors.toList());

        StringBuilder script = new StringBuilder();

        script.append("<style>");

        script.append("@import url('https://fonts.googleapis.com/css2?family=Open+Sans:wght@500&family=Poppins:wght@500&family=Roboto:wght@500&display=swap');");

        script.append("#professorat{");
        script.append("    max-width: 1160px;");
        script.append("}");

        script.append(".professors{");
        script.append("    display: flex !important;");
        script.append("    flex-wrap: wrap !important;");
        script.append("    justify-content: center;");
        script.append("    gap: 20px;");
        script.append("}");

        script.append(".professor{");
        script.append("     background: #F6F6F6;");
        script.append("     padding: 10px;");
        script.append("     width: 270px;");
        script.append("     display: flex;");
        script.append("     flex-flow: column;");
        script.append("     align-items: center;");
        script.append("     justify-content: start;");
        script.append("}");

        script.append(".professor .informacio{");
        script.append("     display: flex;");
        script.append("     flex-flow: column;");
        script.append("     align-items: center;");
        script.append("     justify-content: space-between;");
        script.append("     height: 100%;");
        script.append("}");

        script.append(".professor .foto{");
        script.append("     width: 235px;");
        script.append("     height: 235px;");
        script.append("     position: relative;");
        script.append("}");

        script.append(".professor .foto-titular{");
        script.append("     width: 80px;");
        script.append("     height: 80px;");
        script.append("     filter: grayscale(1);");
        script.append("     position: absolute;");
        script.append("     bottom: 3px;");
        script.append("     right: 3px;");
        script.append("     margin: 0px;");
        script.append("     padding: 0px;");
        script.append("}");

        script.append(".professor .foto-titular img{");
        script.append("     border-radius: 2px;");
        script.append("}");

        script.append(".professor .foto img{");
        script.append("     object-fit: cover;");
        script.append("}");

        script.append(".professor h3{");
        script.append("     font-family: \"Roboto\", Sans-serif;");
        script.append("     font-size: 1.2em;");
        script.append("     font-weight: 500;");
        script.append("     text-align: center;");
        script.append("     margin: 10px 0;");
        script.append("     color: #54595F;");
        script.append("}");

        script.append(".professor .carrecs{");
        script.append("     width: 100%;");
        script.append("     color: #9E2050;");
        script.append("     font-family: \"Open Sans\", Sans-serif;");
        script.append("     font-size: 0.8em;");
        script.append("     font-weight: 500;");
        script.append("     text-align: center;");
        script.append("     border-bottom: solid 1px #9E2050;");
        script.append("}");

        script.append(".professor .horaritutoria{");
        script.append("     color: #54595f;");
        script.append("     font-family: \"Poppins\", Sans-serif;");
        script.append("     font-size: 0.8em;");
        script.append("     font-weight: 500;");
        script.append("     text-align: center;");
        script.append("}");

        script.append(".professor .nomsubstitut{");
        script.append("     color: #54595f;");
        script.append("     font-family: \"Montserrat\", Sans-serif;");
        script.append("     font-size: 13px;");
        script.append("     font-weight: normal;");
        script.append("     text-align: center;");
        script.append("}");


        script.append(".professor .email{");
        script.append("     color: #54595F;");
        script.append("     font-family: \"Roboto\", Sans-serif;");
        script.append("     font-size: 0.8em;");
        script.append("     font-weight: 500;");
        script.append("     text-align: center;");
        script.append("}");


        script.append("</style>");

        script.append("<div class=\"professors elementor-container elementor-column-gap-default\">");
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

                    script.append("<div class=\"professor\">");

                    //Foto
                    if (usuari.getFoto() != null) {
                        if(usuariSubstitut==null) {
                            script.append("<div class=\"foto\">");
                            script.append("<figure><img src=\"https://www.iesmanacor.cat/wp-content/uploads/FOTOS/").append(usuari.getFoto()).append("\" alt=\"\"></figure>");
                            script.append("</div>");
                        } else {
                            script.append("<div class=\"foto\">");
                            script.append("<figure class=\"foto-substitut\"><img src=\"https://www.iesmanacor.cat/wp-content/uploads/FOTOS/").append(usuariSubstitut.getFoto()).append("\" alt=\"\"></figure>");
                            script.append("<figure class=\"foto-titular\"><img src=\"https://www.iesmanacor.cat/wp-content/uploads/FOTOS/").append(usuari.getFoto()).append("\" alt=\"\"></figure>");
                            script.append("</div>");
                        }
                    }

                    script.append("<div class=\"informacio\">");

                    //Nom i cognoms
                    if(usuariSubstitut==null) {
                        script.append("<h3>").append(usuari.getNom()).append("</h3>");
                    } else {
                        script.append("<h3>").append(usuariSubstitut.getNom()).append("</h3>");
                        script.append("<p class=\"nomsubstitut\">(Substitueix a ").append(usuari.getNom()).append(")</p>");
                    }
                    //Càrrecs
                    script.append("<div class=\"carrecs\">");
                    if (usuari.getTutoria() != null && !usuari.getTutoria().isEmpty()) {
                        script.append("Tutoria ").append(usuari.getTutoria());
                    }
                    if (usuari.getCarrec1() != null && !usuari.getCarrec1().isEmpty()) {
                        if (usuari.getTutoria() != null && !usuari.getTutoria().isEmpty()) {
                            script.append("<br>");
                        }
                        script.append(usuari.getCarrec1());
                    }
                    if (usuari.getCarrec2() != null && !usuari.getCarrec2().isEmpty()) {
                        script.append("<br>").append(usuari.getCarrec2());
                    }
                    if (usuari.getCarrec3() != null && !usuari.getCarrec3().isEmpty()) {
                        script.append("<br>").append(usuari.getCarrec3());
                    }
                    script.append("</div>");

                    //Horari tutoria
                    script.append("<p class=\"horaritutoria\">").append(usuari.getHorariAtencioPares()).append("</p>");

                    if(usuariSubstitut==null) {
                        if (usuari.getProfessor().getGsuiteEmail() != null) {
                            script.append("<p class=\"email\"><a href=\"mailto:").append(usuari.getProfessor().getGsuiteEmail()).append("\">");
                            script.append(usuari.getProfessor().getGsuiteEmail());
                            script.append("</a></p>");
                        }
                    } else {
                        if (usuariSubstitut.getProfessor().getGsuiteEmail() != null) {
                            script.append("<p class=\"email\"><a href=\"mailto:").append(usuariSubstitut.getProfessor().getGsuiteEmail()).append("\">");
                            script.append(usuariSubstitut.getProfessor().getGsuiteEmail());
                            script.append("</a></p>");
                        }
                    }

                    script.append("</div>"); //class informació

                    script.append("</div>"); //class professor
                }
            }
        }
        script.append("</div>"); //professors

        String selector = "#professorat";

        String result = " document.querySelector(\""+selector+"\").innerHTML=''; ";
        result += " var resultScript = document.querySelector(\""+selector+"\"); ";
        result += " if(resultScript){ resultScript.innerHTML = `" + script + "`; } ";

        return result;
    }

}