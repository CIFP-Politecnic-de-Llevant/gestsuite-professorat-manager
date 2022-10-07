package cat.iesmanacor.webiesmanacor.restclient;

import cat.iesmanacor.webiesmanacor.dto.CoreUsuariDto;
import cat.iesmanacor.webiesmanacor.dto.DepartamentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@FeignClient(name = "core")
public interface CoreRestClient {

    //USUARIS
    @GetMapping("/usuaris/profile/{id}")
    ResponseEntity<CoreUsuariDto> getProfile(@PathVariable("id") String idUsuari) throws Exception;

    @GetMapping("/usuaris/llistat/actius")
    ResponseEntity<List<CoreUsuariDto>> getUsuarisActius();

    @GetMapping("/usuaris/findByDepartament/{id}")
    ResponseEntity<List<CoreUsuariDto>> getUsuarisByDepartament(@PathVariable("id") Long idDepartament);

    //Departaments
    @GetMapping("/departament/getById/{id}")
    ResponseEntity<DepartamentDto> getDepartamentById(@PathVariable("id") Long identificador);

    @GetMapping("/departament/getByCodiGestib/{id}")
    ResponseEntity<DepartamentDto> getDepartamentByCodiGestib(@PathVariable("id") String identificador);

}
