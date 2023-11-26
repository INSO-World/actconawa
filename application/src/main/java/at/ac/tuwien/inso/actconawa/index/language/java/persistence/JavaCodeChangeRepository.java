package at.ac.tuwien.inso.actconawa.index.language.java.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JavaCodeChangeRepository extends JpaRepository<JavaCodeChange, UUID> {

}
