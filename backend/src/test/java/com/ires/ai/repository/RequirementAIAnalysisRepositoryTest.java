package com.ires.ai.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RequirementAIAnalysisRepositoryTest {

    @Autowired
    private RequirementAIAnalysisRepository analysisRepository;

    @Test
    void repositoryBeanIsAvailable() {
        assertThat(analysisRepository).isNotNull();
    }
}
