package ru.nsu.digitallibrary.repository.postgres;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.digitallibrary.entity.postgres.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
}

