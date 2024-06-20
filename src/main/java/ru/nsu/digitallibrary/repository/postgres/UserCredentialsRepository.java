package ru.nsu.digitallibrary.repository.postgres;

import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.digitallibrary.entity.postgres.UserCredentials;

@Repository
public interface UserCredentialsRepository extends JpaRepository<UserCredentials, Long> {
    UserCredentials findByUsername(@Size(min = 2, message = "Не меньше 5 знаков") String username);

    boolean existsByUsername(@Size(min = 2, message = "Не меньше 5 знаков") String username);
}

