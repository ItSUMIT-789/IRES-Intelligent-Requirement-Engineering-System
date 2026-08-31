package com.ires.testing.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TestCaseRepositoryTest {

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private TestCaseExecutionRepository executionRepository;

    @Test
    void repositoriesSupportTestCasesAndExecutions() {
        assertThat(testCaseRepository).isInstanceOf(JpaSpecificationExecutor.class);
        assertThat(executionRepository).isNotNull();
    }
}
