package com.ires.traceability.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TraceabilityLinkRepositoryTest {

    @Autowired
    private TraceabilityLinkRepository linkRepository;

    @Test
    void repositoryBeanIsAvailable() {
        assertThat(linkRepository).isNotNull();
    }
}
