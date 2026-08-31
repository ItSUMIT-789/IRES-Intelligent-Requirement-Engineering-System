package com.ires.requirement.attachment.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RequirementAttachmentRepositoryTest {

    @Autowired
    private RequirementAttachmentRepository attachmentRepository;

    @Test
    void repositoryBeanIsAvailableForPagedQueries() {
        assertThat(attachmentRepository).isNotNull();
    }
}
