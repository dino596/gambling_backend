package com.nighthawk.spring_portfolio.mvc.userdb;

import org.springframework.data.jpa.repository.JpaRepository;

public interface  UserdbRoleJpaRepository extends JpaRepository<UserdbRole, Long> {
    UserdbRole findByName(String name);
}
