package cat.politecnicllevant.professoratmanager.repository;

import cat.politecnicllevant.professoratmanager.model.Usuari;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuariRepository extends JpaRepository<Usuari, Long> {
    Usuari findUsuariByProfessor(Long idProfessor);
    Usuari findUsuariBySubstitut(Usuari usuariSubstitut);
}
