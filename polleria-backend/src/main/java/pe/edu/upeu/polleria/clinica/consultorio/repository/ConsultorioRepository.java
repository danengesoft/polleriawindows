package pe.edu.upeu.polleria.clinica.consultorio.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.polleria.clinica.consultorio.entity.Consultorio;

public interface ConsultorioRepository extends JpaRepository<Consultorio, Long> {

    // Trae la clínica y las especialidades en la misma consulta (evita el problema N+1).
    @Override
    @EntityGraph(attributePaths = {"clinica", "especialidades"})
    List<Consultorio> findAll(Sort sort);

    @Override
    @EntityGraph(attributePaths = {"clinica", "especialidades"})
    Optional<Consultorio> findById(Long id);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByClinicaId(Long clinicaId);
}
