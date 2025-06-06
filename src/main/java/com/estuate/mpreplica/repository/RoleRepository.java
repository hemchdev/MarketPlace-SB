package com.estuate.mpreplica.repository;


import com.estuate.mpreplica.entity.Role;
import com.estuate.mpreplica.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}

