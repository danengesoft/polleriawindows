package pe.edu.upeu.polleria.clinica.especialidad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.polleria.clinica.especialidad.entity.Especialidad;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {
}
