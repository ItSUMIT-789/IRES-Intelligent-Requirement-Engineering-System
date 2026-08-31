package com.ires.requirement.criteria.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AcceptanceCriteriaRepositoryTest {

    @Autowired
    private AcceptanceCriteriaRepository criteriaRepository;

    @Test
    void repositoryBeanIsAvailableForPagedQueries() {
        assertThat(criteriaRepository).isNotNull();
    }
}
