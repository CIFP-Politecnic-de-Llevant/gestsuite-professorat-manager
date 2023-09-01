package cat.politecnicllevant.professoratmanager.service;

import cat.politecnicllevant.professoratmanager.dto.CoreUsuariDto;
import cat.politecnicllevant.professoratmanager.dto.DepartamentDto;
import cat.politecnicllevant.professoratmanager.dto.UsuariDto;
import cat.politecnicllevant.professoratmanager.model.Usuari;
import cat.politecnicllevant.professoratmanager.repository.UsuariRepository;
import cat.politecnicllevant.professoratmanager.restclient.CoreRestClient;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuariService {
    @Autowired
    private UsuariRepository usuariRepository;

    @Autowired
    private CoreRestClient coreRestClient;


    public List<UsuariDto> findAll(){
        ModelMapper modelMapper = new ModelMapper();
        return usuariRepository.findAll().stream().map(c->modelMapper.map(c,UsuariDto.class)).collect(Collectors.toList());
    }

    public UsuariDto findByCoreIdUsuari(Long coreIdUsuari) throws Exception {
        ModelMapper modelMapper = new ModelMapper();
        Usuari usuari = usuariRepository.findUsuariByProfessor(coreIdUsuari);
        if(usuari!=null) {
            UsuariDto usuariDto = modelMapper.map(usuari, UsuariDto.class);
            ResponseEntity<CoreUsuariDto> coreUsuariDtoResponseEntity = coreRestClient.getPublicProfile(coreIdUsuari.toString());
            CoreUsuariDto coreUsuariDto = coreUsuariDtoResponseEntity.getBody();
            usuariDto.setProfessor(coreUsuariDto);

            return usuariDto;
        } else{
            return null;
        }
    }

    public boolean usuariIsSubstitut(Long idUsuari) throws Exception {
        Usuari usuari = usuariRepository.findById(idUsuari).orElse(null);
        if(usuari!=null){
            Usuari substitut = usuariRepository.findUsuariBySubstitut(usuari);
            return substitut != null;
        } else{
            return false;
        }
    }

    public UsuariDto findById(Long idUsuari) throws Exception {
        ModelMapper modelMapper = new ModelMapper();
        Usuari usuari = usuariRepository.findById(idUsuari).orElse(null);
        if(usuari!=null) {
            UsuariDto usuariDto = modelMapper.map(usuari, UsuariDto.class);

            ResponseEntity<CoreUsuariDto> professorResponse = coreRestClient.getPublicProfile(usuari.getProfessor().toString());
            CoreUsuariDto professor = professorResponse.getBody();

            if(professor!=null) {
               usuariDto.setProfessor(professor);
            }

            if(usuari.getDepartament() != null) {
                ResponseEntity<DepartamentDto> departamentResponse = coreRestClient.getDepartamentByCodiGestib(usuari.getDepartament().toString());
                DepartamentDto departament = departamentResponse.getBody();

                if (departament != null) {
                    usuariDto.setDepartament(departament);
                }
            }

            return usuariDto;
        } else{
            return null;
        }
    }

    @Transactional
    public UsuariDto save(UsuariDto u){
        CoreUsuariDto professor = u.getProfessor();
        DepartamentDto departament = u.getDepartament();

        ModelMapper modelMapper = new ModelMapper();

        Usuari usuari = modelMapper.map(u,Usuari.class);
        //Afegeim el professor manualment perquè el mapeig no és igual Usuari i UsuariDTO
        usuari.setProfessor(professor.getIdusuari());

        //Afegim el departament manualment perquè el mateig no és igual Usuari i UsuariDTO
        if(departament!=null) {
            usuari.setDepartament(departament.getIddepartament());
        }

        Usuari usuariSaved = usuariRepository.save(usuari);

        UsuariDto usuariDtoSaved = modelMapper.map(usuariSaved,UsuariDto.class);
        //Afegeim el professor manualment perquè el mapeig no és igual Usuari i UsuariDTO
        usuariDtoSaved.setProfessor(professor);

        return usuariDtoSaved;
    }

}

