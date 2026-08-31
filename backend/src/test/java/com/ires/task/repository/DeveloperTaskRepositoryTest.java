package com.ires.task.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DeveloperTaskRepositoryTest {

    @Autowired
    private DeveloperTaskRepository taskRepository;

    @Test
    void supportsSpecificationFilteringAndPaging() {
        assertThat(taskRepository).isInstanceOf(JpaSpecificationExecutor.class);
    }
}
