package com.ires.project.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void repositorySupportsSpecificationsAndPaging() {
        assertThat(projectRepository).isInstanceOf(org.springframework.data.jpa.repository.JpaSpecificationExecutor.class);
    }
}
