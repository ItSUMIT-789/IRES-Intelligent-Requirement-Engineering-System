package com.ires.user.repository;

import com.ires.user.entity.Role;
import com.ires.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void findsUserByEmailAndChecksEmailExistence() {
        Role role = roleRepository.findByName("CLIENT").orElseThrow();
        User user = new User("Ada", "Lovelace", "ada@example.com", "hashed-password", role);

        userRepository.saveAndFlush(user);

        assertThat(userRepository.findByEmail("ada@example.com")).contains(user);
        assertThat(userRepository.existsByEmail("ada@example.com")).isTrue();
    }
}
