package cat.iesmanacor.webiesmanacordepartaments.repository;

import cat.iesmanacor.webiesmanacordepartaments.model.Usuari;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuariRepository extends JpaRepository<Usuari, Long> {
    Usuari findUsuariByProfessor(Long idProfessor);
}
