package com.ires.story.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserStoryRepositoryTest {

    @Autowired
    private UserStoryRepository userStoryRepository;

    @Test
    void repositoryBeanIsAvailable() {
        assertThat(userStoryRepository).isNotNull();
    }
}
