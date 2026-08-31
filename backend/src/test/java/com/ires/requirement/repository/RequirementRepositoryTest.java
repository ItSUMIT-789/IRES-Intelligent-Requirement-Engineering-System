package com.ires.requirement.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RequirementRepositoryTest {

    @Autowired
    private RequirementRepository requirementRepository;

    @Test
    void supportsSpecificationQueriesForFilteredPaging() {
        assertThat(requirementRepository).isInstanceOf(JpaSpecificationExecutor.class);
    }
}
