package com.company.learninghub.studymaterial.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.storage.StorageProperties;
import com.company.learninghub.storage.StudyMaterialStorageService;
import com.company.learninghub.studymaterial.mapper.StudyMaterialMapper;
import com.company.learninghub.studymaterial.repository.StudyMaterialDownloadEventRepository;
import com.company.learninghub.studymaterial.repository.StudyMaterialFolderRepository;
import com.company.learninghub.studymaterial.repository.StudyMaterialRepository;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = StudyMaterialMethodSecurityTest.TestConfig.class)
class StudyMaterialMethodSecurityTest {

    @Autowired
    private StudyMaterialService studyMaterialService;

    @Autowired
    private StudyMaterialFolderRepository folderRepository;

    @Autowired
    private StudyMaterialRepository materialRepository;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCannotMutateStudyMaterialRepository() {
        MockMultipartFile file = new MockMultipartFile("file", "guide.pdf", "application/pdf", "pdf".getBytes());
        assertThatThrownBy(() -> studyMaterialService.uploadFileMaterial(
                null,
                "Guide",
                null,
                null,
                file,
                principal(RoleName.EMPLOYEE)
        ))
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> studyMaterialService.deleteFolder(UUID.randomUUID()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCanBrowseAndSearchStudyMaterials() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(folderRepository.findByParentId(eq(null), any()))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        when(materialRepository.search(eq(null), eq(null), eq(null), any()))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        assertThat(studyMaterialService.listFolders(null, pageable).getTotalElements()).isZero();
        assertThat(studyMaterialService.searchMaterials(null, null, null, pageable).getTotalElements()).isZero();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanBrowseAndSearchStudyMaterials() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(folderRepository.findByParentId(eq(null), any()))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        when(materialRepository.search(eq(null), eq(null), eq(null), any()))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        assertThat(studyMaterialService.listFolders(null, pageable).getTotalElements()).isZero();
        assertThat(studyMaterialService.searchMaterials(null, null, null, pageable).getTotalElements()).isZero();
    }

    private AuthenticatedUser principal(RoleName roleName) {
        User user = new User(roleName.name() + "001", roleName.name().toLowerCase() + "@learninghub.local", roleName.name(), "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(roleName));
        return AuthenticatedUser.from(user);
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        StudyMaterialService studyMaterialService(
                StudyMaterialFolderRepository folderRepository,
                StudyMaterialRepository materialRepository,
                StudyMaterialDownloadEventRepository downloadEventRepository,
                UserRepository userRepository,
                StudyMaterialStorageService storageService,
                StorageProperties storageProperties,
                StudyMaterialMapper mapper
        ) {
            return new StudyMaterialService(
                    folderRepository,
                    materialRepository,
                    downloadEventRepository,
                    userRepository,
                    storageService,
                    storageProperties,
                    mapper
            );
        }

        @Bean
        StudyMaterialFolderRepository studyMaterialFolderRepository() {
            return mock(StudyMaterialFolderRepository.class);
        }

        @Bean
        StudyMaterialRepository studyMaterialRepository() {
            return mock(StudyMaterialRepository.class);
        }

        @Bean
        StudyMaterialDownloadEventRepository studyMaterialDownloadEventRepository() {
            return mock(StudyMaterialDownloadEventRepository.class);
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        StudyMaterialStorageService studyMaterialStorageService() {
            return mock(StudyMaterialStorageService.class);
        }

        @Bean
        StorageProperties storageProperties() {
            StorageProperties storageProperties = new StorageProperties();
            storageProperties.setMaxFileSizeBytes(1024);
            return storageProperties;
        }

        @Bean
        StudyMaterialMapper studyMaterialMapper() {
            return new StudyMaterialMapper();
        }
    }
}

