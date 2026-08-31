package com.ires.bug.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BugRepositoryTest {

    @Autowired
    private BugRepository bugRepository;

    @Test
    void supportsFilteredPaging() {
        assertThat(bugRepository).isInstanceOf(JpaSpecificationExecutor.class);
    }
}
