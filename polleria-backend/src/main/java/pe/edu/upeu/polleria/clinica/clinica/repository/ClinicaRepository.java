package pe.edu.upeu.polleria.clinica.clinica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.polleria.clinica.clinica.entity.Clinica;

public interface ClinicaRepository extends JpaRepository<Clinica, Long> {

    boolean existsByRuc(String ruc);

    boolean existsByRucAndIdNot(String ruc, Long id);
}
