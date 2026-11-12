package org.example.repository;

import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // 2. Проверка, существует ли пользователь с таким email (вернет boolean)
    boolean existsByEmail(String email);

    // 3. Поиск по имени (вернет список, так как имен может быть много)
    List<User> findByName(String name);

    // 4. Поиск по частичному совпадению (SQL: WHERE name LIKE '%иван%')
    List<User> findByNameContainingIgnoreCase(String namePart);

    // 5. Поиск по имени и email одновременно (SQL: WHERE name = ? AND email = ?)
    Optional<User> findByNameAndEmail(String name, String email);

    // 6. Сортировка (SQL: ORDER BY created_at DESC)
    List<User> findAllByOrderByCreatedAtDesc();

    // JPQL запрос
    @Query("SELECT u FROM User u WHERE u.email LIKE %:domain")
    List<User> findUsersByEmailDomain(@Param("domain") String domain);

    // Чистый SQL
    @Query(value = "SELECT * FROM users WHERE created_at > :date", nativeQuery = true)
    List<User> findUsersCreatedAfter(@Param("date") String date);

    @Query(value = "SELECT * FROM users WHERE email LIKE %?1", nativeQuery = true)
    List<User> findUsersByEmailDomainNative(String domain);
}
