package com.company.learninghub.studymaterial.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.storage.StorageProperties;
import com.company.learninghub.storage.StoredFile;
import com.company.learninghub.storage.StudyMaterialStorageService;
import com.company.learninghub.studymaterial.domain.MaterialSourceType;
import com.company.learninghub.studymaterial.domain.MaterialType;
import com.company.learninghub.studymaterial.domain.StudyMaterial;
import com.company.learninghub.studymaterial.domain.StudyMaterialDownloadEvent;
import com.company.learninghub.studymaterial.domain.StudyMaterialFolder;
import com.company.learninghub.studymaterial.dto.CreateFolderRequest;
import com.company.learninghub.studymaterial.dto.CreateLinkMaterialRequest;
import com.company.learninghub.studymaterial.dto.LinkDownloadResponse;
import com.company.learninghub.studymaterial.dto.StudyMaterialFolderResponse;
import com.company.learninghub.studymaterial.dto.StudyMaterialResponse;
import com.company.learninghub.studymaterial.dto.UpdateFolderRequest;
import com.company.learninghub.studymaterial.dto.UpdateMaterialRequest;
import com.company.learninghub.studymaterial.mapper.StudyMaterialMapper;
import com.company.learninghub.studymaterial.repository.StudyMaterialDownloadEventRepository;
import com.company.learninghub.studymaterial.repository.StudyMaterialFolderRepository;
import com.company.learninghub.studymaterial.repository.StudyMaterialRepository;
import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyMaterialServiceTest {

    @Mock
    private StudyMaterialFolderRepository folderRepository;

    @Mock
    private StudyMaterialRepository materialRepository;

    @Mock
    private StudyMaterialDownloadEventRepository downloadEventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudyMaterialStorageService storageService;

    private StorageProperties storageProperties;
    private StudyMaterialService studyMaterialService;
    private User admin;
    private AuthenticatedUser adminPrincipal;
    private StudyMaterialFolder rootFolder;

    @BeforeEach
    void setUp() {
        storageProperties = new StorageProperties();
        storageProperties.setMaxFileSizeBytes(1024);
        studyMaterialService = new StudyMaterialService(
                folderRepository,
                materialRepository,
                downloadEventRepository,
                userRepository,
                storageService,
                storageProperties,
                new StudyMaterialMapper()
        );
        admin = user(RoleName.ADMIN);
        adminPrincipal = AuthenticatedUser.from(admin);
        rootFolder = folder("AI Certifications", null, admin);
    }

    @Test
    void createFolderCreatesRootFolderForAdmin() {
        CreateFolderRequest request = new CreateFolderRequest(" AI Certifications ", "  certification docs ", null);
        when(folderRepository.existsSiblingWithName("AI Certifications", null, null)).thenReturn(false);
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(folderRepository.save(any(StudyMaterialFolder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudyMaterialFolderResponse response = studyMaterialService.createFolder(request, adminPrincipal);

        assertThat(response.name()).isEqualTo("AI Certifications");
        assertThat(response.description()).isEqualTo("certification docs");
        assertThat(response.parentId()).isNull();
        assertThat(response.createdBy().id()).isEqualTo(admin.getId());
    }

    @Test
    void updateFolderRejectsSelfParentAndDuplicateSiblingName() {
        UUID folderId = rootFolder.getId();
        assertThatThrownBy(() -> studyMaterialService.updateFolder(
                folderId,
                new UpdateFolderRequest("AI", null, folderId)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Folder cannot be its own parent");

        when(folderRepository.findById(folderId)).thenReturn(Optional.of(rootFolder));
        when(folderRepository.existsSiblingWithName("AI Certifications", null, folderId)).thenReturn(true);

        assertThatThrownBy(() -> studyMaterialService.updateFolder(
                folderId,
                new UpdateFolderRequest("AI Certifications", null, null)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A folder with this name already exists at this level");
    }

    @Test
    void updateFolderRejectsMovingFolderUnderDescendant() {
        StudyMaterialFolder child = folder("Anthropic", rootFolder, admin);
        when(folderRepository.findById(rootFolder.getId())).thenReturn(Optional.of(rootFolder));
        when(folderRepository.findById(child.getId())).thenReturn(Optional.of(child));

        assertThatThrownBy(() -> studyMaterialService.updateFolder(
                rootFolder.getId(),
                new UpdateFolderRequest("AI Certifications", null, child.getId())
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Folder cannot be moved under its descendant");
    }

    @Test
    void listFoldersSupportsNestedFolderBrowsingAndSortTranslation() {
        StudyMaterialFolder child = folder("Anthropic", rootFolder, admin);
        PageRequest pageable = PageRequest.of(1, 10, Sort.by(Sort.Order.desc("createdAtUtc")));
        when(folderRepository.findByParentId(eq(rootFolder.getId()), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(child), invocation.getArgument(1), 1));

        studyMaterialService.listFolders(rootFolder.getId(), pageable);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(folderRepository).findByParentId(eq(rootFolder.getId()), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("createdAt")).isNotNull();
    }

    @Test
    void deleteFolderRequiresEmptyFolder() {
        when(folderRepository.findById(rootFolder.getId())).thenReturn(Optional.of(rootFolder));
        when(folderRepository.existsByParentId(rootFolder.getId())).thenReturn(true);

        assertThatThrownBy(() -> studyMaterialService.deleteFolder(rootFolder.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Folder must be empty before deletion");

        verify(folderRepository, never()).delete(any());
    }

    @Test
    void deleteFolderRejectsFolderContainingMaterials() {
        when(folderRepository.findById(rootFolder.getId())).thenReturn(Optional.of(rootFolder));
        when(folderRepository.existsByParentId(rootFolder.getId())).thenReturn(false);
        when(materialRepository.existsByFolderId(rootFolder.getId())).thenReturn(true);

        assertThatThrownBy(() -> studyMaterialService.deleteFolder(rootFolder.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Folder must be empty before deletion");

        verify(folderRepository, never()).delete(any());
    }

    @Test
    void uploadFileMaterialStoresFileAndCreatesMaterial() {
        MockMultipartFile file = pdfFile();
        StoredFile storedFile = new StoredFile("LOCAL", "study-materials/file.pdf", "guide.pdf", "application/pdf", file.getSize());
        when(folderRepository.findById(rootFolder.getId())).thenReturn(Optional.of(rootFolder));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(storageService.store(file)).thenReturn(storedFile);
        when(materialRepository.save(any(StudyMaterial.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudyMaterialResponse response = studyMaterialService.uploadFileMaterial(
                rootFolder.getId(),
                " AWS Guide ",
                " Prep guide ",
                MaterialType.PDF,
                file,
                adminPrincipal
        );

        assertThat(response.title()).isEqualTo("AWS Guide");
        assertThat(response.materialType()).isEqualTo(MaterialType.PDF);
        assertThat(response.sourceType()).isEqualTo(MaterialSourceType.FILE);
        assertThat(response.originalFilename()).isEqualTo("guide.pdf");
        verify(storageService).store(file);
    }

    @Test
    void uploadFileMaterialRejectsMismatchedTypeAndOversizedFile() {
        MockMultipartFile pptAsPdf = new MockMultipartFile(
                "file",
                "slides.pptx",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "slides".getBytes()
        );

        assertThatThrownBy(() -> studyMaterialService.uploadFileMaterial(
                null,
                "Slides",
                null,
                MaterialType.PDF,
                pptAsPdf,
                adminPrincipal
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Study material file type does not match the selected material type");

        MockMultipartFile large = new MockMultipartFile("file", "guide.pdf", "application/pdf", new byte[2048]);
        assertThatThrownBy(() -> studyMaterialService.uploadFileMaterial(
                null,
                "Guide",
                null,
                MaterialType.PDF,
                large,
                adminPrincipal
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Study material file exceeds the configured maximum size");
    }

    @Test
    void uploadFileMaterialRejectsEmptyFile() {
        MockMultipartFile empty = new MockMultipartFile("file", "guide.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> studyMaterialService.uploadFileMaterial(
                null,
                "Guide",
                null,
                MaterialType.PDF,
                empty,
                adminPrincipal
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Study material file is required");

        verify(storageService, never()).store(any());
    }

    @Test
    void uploadFileMaterialRejectsInvalidFolderBeforeStoringFile() {
        UUID missingFolderId = UUID.randomUUID();
        when(folderRepository.findById(missingFolderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyMaterialService.uploadFileMaterial(
                missingFolderId,
                "Guide",
                null,
                MaterialType.PDF,
                pdfFile(),
                adminPrincipal
        ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Study material folder was not found");

        verify(storageService, never()).store(any());
    }

    @Test
    void uploadFileMaterialDeletesStoredFileIfPersistenceFails() {
        MockMultipartFile file = pdfFile();
        StoredFile storedFile = new StoredFile("LOCAL", "study-materials/orphan.pdf", "guide.pdf", "application/pdf", file.getSize());
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(storageService.store(file)).thenReturn(storedFile);
        when(materialRepository.save(any(StudyMaterial.class))).thenThrow(new IllegalStateException("db unavailable"));

        assertThatThrownBy(() -> studyMaterialService.uploadFileMaterial(
                null,
                "Guide",
                null,
                MaterialType.PDF,
                file,
                adminPrincipal
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("db unavailable");

        verify(storageService).deleteQuietly("study-materials/orphan.pdf");
    }

    @Test
    void createLinkMaterialRequiresLinkTypeAndHttpUrl() {
        assertThatThrownBy(() -> studyMaterialService.createLinkMaterial(
                new CreateLinkMaterialRequest(null, "Bad", null, MaterialType.PDF, "https://example.com"),
                adminPrincipal
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Link material type must be VIDEO_LINK or EXTERNAL_LINK");

        assertThatThrownBy(() -> studyMaterialService.createLinkMaterial(
                new CreateLinkMaterialRequest(null, "Bad", null, MaterialType.EXTERNAL_LINK, "ftp://example.com"),
                adminPrincipal
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("External URL must start with http:// or https://");
    }

    @Test
    void createLinkMaterialRejectsInvalidFolder() {
        UUID missingFolderId = UUID.randomUUID();
        when(folderRepository.findById(missingFolderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyMaterialService.createLinkMaterial(
                new CreateLinkMaterialRequest(missingFolderId, "Docs", null, MaterialType.EXTERNAL_LINK, "https://example.com"),
                adminPrincipal
        ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Study material folder was not found");

        verify(materialRepository, never()).save(any());
    }

    @Test
    void createAndUpdateLinkMaterial() {
        CreateLinkMaterialRequest request = new CreateLinkMaterialRequest(
                rootFolder.getId(),
                "OpenAI Docs",
                "Official docs",
                MaterialType.EXTERNAL_LINK,
                "https://example.com/docs"
        );
        StudyMaterial material = linkMaterial(rootFolder, admin);
        when(folderRepository.findById(rootFolder.getId())).thenReturn(Optional.of(rootFolder));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(materialRepository.save(any(StudyMaterial.class))).thenReturn(material);

        StudyMaterialResponse created = studyMaterialService.createLinkMaterial(request, adminPrincipal);

        assertThat(created.sourceType()).isEqualTo(MaterialSourceType.LINK);

        when(materialRepository.findById(material.getId())).thenReturn(Optional.of(material));
        StudyMaterialResponse updated = studyMaterialService.updateMaterial(
                material.getId(),
                new UpdateMaterialRequest(null, "Updated", "Updated desc", "https://example.com/updated")
        );

        assertThat(updated.title()).isEqualTo("Updated");
        assertThat(updated.externalUrl()).isEqualTo("https://example.com/updated");
    }

    @Test
    void searchMaterialsNormalizesSortAndSearch() {
        PageRequest pageable = PageRequest.of(1, 5, Sort.by(Sort.Order.desc("createdAtUtc")));
        StudyMaterial material = fileMaterial(rootFolder, admin);
        when(materialRepository.search(eq(rootFolder.getId()), eq(MaterialType.PDF), eq("AWS"), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(material), invocation.getArgument(3), 1));

        studyMaterialService.searchMaterials(rootFolder.getId(), MaterialType.PDF, "  AWS  ", pageable);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(materialRepository).search(eq(rootFolder.getId()), eq(MaterialType.PDF), eq("AWS"), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("createdAt")).isNotNull();

        assertThatThrownBy(() -> studyMaterialService.searchMaterials(null, null, null, PageRequest.of(0, 20, Sort.by("badField"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported sort property: badField");
    }

    @Test
    void downloadFileTracksDownloadAndLoadsResource() {
        StudyMaterial material = fileMaterial(rootFolder, admin);
        when(materialRepository.findById(material.getId())).thenReturn(Optional.of(material));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(storageService.loadAsResource(material.getStorageKey())).thenReturn(new ByteArrayResource("content".getBytes()));

        studyMaterialService.downloadFileMaterial(material.getId(), adminPrincipal);
        studyMaterialService.downloadFileMaterial(material.getId(), adminPrincipal);

        assertThat(material.getDownloadCount()).isEqualTo(2);
        verify(downloadEventRepository, org.mockito.Mockito.times(2)).save(any(StudyMaterialDownloadEvent.class));
        verify(storageService, org.mockito.Mockito.times(2)).loadAsResource(material.getStorageKey());
    }

    @Test
    void accessLinkTracksDownload() {
        StudyMaterial material = linkMaterial(rootFolder, admin);
        when(materialRepository.findById(material.getId())).thenReturn(Optional.of(material));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        LinkDownloadResponse response = studyMaterialService.accessLinkMaterial(material.getId(), adminPrincipal);
        LinkDownloadResponse secondResponse = studyMaterialService.accessLinkMaterial(material.getId(), adminPrincipal);

        assertThat(response.externalUrl()).isEqualTo(material.getExternalUrl());
        assertThat(response.downloadCount()).isEqualTo(1);
        assertThat(secondResponse.downloadCount()).isEqualTo(2);
        verify(downloadEventRepository, org.mockito.Mockito.times(2)).save(any(StudyMaterialDownloadEvent.class));
    }

    @Test
    void deleteMaterialDeletesStoredFileForFileMaterials() {
        StudyMaterial material = fileMaterial(rootFolder, admin);
        when(materialRepository.findById(material.getId())).thenReturn(Optional.of(material));

        studyMaterialService.deleteMaterial(material.getId());

        verify(materialRepository).delete(material);
        verify(storageService).deleteQuietly(material.getStorageKey());
    }

    private MockMultipartFile pdfFile() {
        return new MockMultipartFile("file", "guide.pdf", "application/pdf", "pdf".getBytes());
    }

    private User user(RoleName roleName) {
        User user = new User(roleName.name() + "001", roleName.name().toLowerCase() + "@learninghub.local", roleName.name(), "$2a$12$hash");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.assignRole(new Role(roleName));
        return user;
    }

    private StudyMaterialFolder folder(String name, StudyMaterialFolder parent, User createdBy) {
        StudyMaterialFolder folder = new StudyMaterialFolder(name, null, parent, createdBy);
        ReflectionTestUtils.setField(folder, "id", UUID.randomUUID());
        return folder;
    }

    private StudyMaterial fileMaterial(StudyMaterialFolder folder, User uploadedBy) {
        StudyMaterial material = StudyMaterial.fileMaterial(
                folder,
                "AWS Guide",
                "Guide",
                MaterialType.PDF,
                "LOCAL",
                "study-materials/file.pdf",
                "guide.pdf",
                "application/pdf",
                10,
                uploadedBy
        );
        ReflectionTestUtils.setField(material, "id", UUID.randomUUID());
        return material;
    }

    private StudyMaterial linkMaterial(StudyMaterialFolder folder, User uploadedBy) {
        StudyMaterial material = StudyMaterial.linkMaterial(
                folder,
                "OpenAI Docs",
                "Docs",
                MaterialType.EXTERNAL_LINK,
                "https://example.com/docs",
                uploadedBy
        );
        ReflectionTestUtils.setField(material, "id", UUID.randomUUID());
        return material;
    }
}

