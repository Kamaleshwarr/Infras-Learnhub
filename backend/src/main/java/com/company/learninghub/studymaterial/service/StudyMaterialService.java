package com.company.learninghub.studymaterial.service;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.exception.ResourceNotFoundException;
import com.company.learninghub.storage.StoredFile;
import com.company.learninghub.storage.StorageProperties;
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
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class StudyMaterialService {

    private static final Set<String> PDF_TYPES = Set.of("application/pdf");
    private static final Set<String> PPT_TYPES = Set.of(
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    );
    private static final Set<String> DOCX_TYPES = Set.of(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final StudyMaterialFolderRepository folderRepository;
    private final StudyMaterialRepository materialRepository;
    private final StudyMaterialDownloadEventRepository downloadEventRepository;
    private final UserRepository userRepository;
    private final StudyMaterialStorageService storageService;
    private final StorageProperties storageProperties;
    private final StudyMaterialMapper mapper;

    public StudyMaterialService(
            StudyMaterialFolderRepository folderRepository,
            StudyMaterialRepository materialRepository,
            StudyMaterialDownloadEventRepository downloadEventRepository,
            UserRepository userRepository,
            StudyMaterialStorageService storageService,
            StorageProperties storageProperties,
            StudyMaterialMapper mapper
    ) {
        this.folderRepository = folderRepository;
        this.materialRepository = materialRepository;
        this.downloadEventRepository = downloadEventRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.storageProperties = storageProperties;
        this.mapper = mapper;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public StudyMaterialFolderResponse createFolder(CreateFolderRequest request, AuthenticatedUser authenticatedUser) {
        StudyMaterialFolder parent = resolveFolder(request.parentId());
        String name = normalizeRequired(request.name(), "Folder name is required");
        ensureUniqueFolderName(name, request.parentId(), null);
        User createdBy = findUser(authenticatedUser.getId());
        StudyMaterialFolder folder = new StudyMaterialFolder(
                name,
                normalizeOptional(request.description()),
                parent,
                createdBy
        );
        return mapper.toFolderResponse(folderRepository.save(folder));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public StudyMaterialFolderResponse updateFolder(UUID folderId, UpdateFolderRequest request) {
        StudyMaterialFolder folder = findFolder(folderId);
        if (folderId.equals(request.parentId())) {
            throw new IllegalArgumentException("Folder cannot be its own parent");
        }
        StudyMaterialFolder parent = resolveFolder(request.parentId());
        String name = normalizeRequired(request.name(), "Folder name is required");
        ensureUniqueFolderName(name, request.parentId(), folderId);
        folder.updateDetails(name, normalizeOptional(request.description()), parent);
        return mapper.toFolderResponse(folder);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteFolder(UUID folderId) {
        StudyMaterialFolder folder = findFolder(folderId);
        if (folderRepository.existsByParentId(folderId)) {
            throw new IllegalArgumentException("Folder must be empty before deletion");
        }
        if (materialRepository.existsByFolderId(folderId)) {
            throw new IllegalArgumentException("Folder must be empty before deletion");
        }
        folderRepository.delete(folder);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public Page<StudyMaterialFolderResponse> listFolders(UUID parentId, Pageable pageable) {
        return folderRepository.findByParentId(parentId, normalizeFolderPageable(pageable))
                .map(mapper::toFolderResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public StudyMaterialResponse uploadFileMaterial(
            UUID folderId,
            String title,
            String description,
            MaterialType materialType,
            MultipartFile file,
            AuthenticatedUser authenticatedUser
    ) {
        validateFileMaterialType(materialType);
        validateFile(file, materialType);
        StudyMaterialFolder folder = resolveFolder(folderId);
        User uploadedBy = findUser(authenticatedUser.getId());
        StoredFile storedFile = storageService.store(file);
        try {
            StudyMaterial material = StudyMaterial.fileMaterial(
                    folder,
                    normalizeRequired(title, "Material title is required"),
                    normalizeOptional(description),
                    materialType,
                    storedFile.storageProvider(),
                    storedFile.storageKey(),
                    storedFile.originalFilename(),
                    storedFile.contentType(),
                    storedFile.fileSizeBytes(),
                    uploadedBy
            );
            return mapper.toMaterialResponse(materialRepository.save(material));
        } catch (RuntimeException ex) {
            storageService.deleteQuietly(storedFile.storageKey());
            throw ex;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public StudyMaterialResponse createLinkMaterial(CreateLinkMaterialRequest request, AuthenticatedUser authenticatedUser) {
        validateLinkMaterialType(request.materialType());
        StudyMaterialFolder folder = resolveFolder(request.folderId());
        User uploadedBy = findUser(authenticatedUser.getId());
        StudyMaterial material = StudyMaterial.linkMaterial(
                folder,
                normalizeRequired(request.title(), "Material title is required"),
                normalizeOptional(request.description()),
                request.materialType(),
                normalizeUrl(request.externalUrl()),
                uploadedBy
        );
        return mapper.toMaterialResponse(materialRepository.save(material));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public StudyMaterialResponse updateMaterial(UUID materialId, UpdateMaterialRequest request) {
        StudyMaterial material = findMaterial(materialId);
        StudyMaterialFolder folder = resolveFolder(request.folderId());
        if (material.isLink()) {
            material.updateLink(
                    normalizeRequired(request.title(), "Material title is required"),
                    normalizeOptional(request.description()),
                    folder,
                    normalizeUrl(request.externalUrl())
            );
        } else {
            material.updateMetadata(
                    normalizeRequired(request.title(), "Material title is required"),
                    normalizeOptional(request.description()),
                    folder
            );
        }
        return mapper.toMaterialResponse(material);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteMaterial(UUID materialId) {
        StudyMaterial material = findMaterial(materialId);
        materialRepository.delete(material);
        if (material.isFile()) {
            storageService.deleteQuietly(material.getStorageKey());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public Page<StudyMaterialResponse> searchMaterials(
            UUID folderId,
            MaterialType materialType,
            String search,
            Pageable pageable
    ) {
        return materialRepository.search(folderId, materialType, normalizeSearch(search), normalizeMaterialPageable(pageable))
                .map(mapper::toMaterialResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional(readOnly = true)
    public StudyMaterialResponse getMaterial(UUID materialId) {
        return mapper.toMaterialResponse(findMaterial(materialId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public Resource downloadFileMaterial(UUID materialId, AuthenticatedUser authenticatedUser) {
        StudyMaterial material = findMaterial(materialId);
        if (!material.isFile()) {
            throw new IllegalArgumentException("Study material is not a downloadable file");
        }
        recordDownload(material, authenticatedUser);
        return storageService.loadAsResource(material.getStorageKey());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public LinkDownloadResponse accessLinkMaterial(UUID materialId, AuthenticatedUser authenticatedUser) {
        StudyMaterial material = findMaterial(materialId);
        if (!material.isLink()) {
            throw new IllegalArgumentException("Study material is not a link");
        }
        recordDownload(material, authenticatedUser);
        return new LinkDownloadResponse(material.getId(), material.getExternalUrl(), material.getDownloadCount());
    }

    private void recordDownload(StudyMaterial material, AuthenticatedUser authenticatedUser) {
        material.incrementDownloadCount();
        downloadEventRepository.save(new StudyMaterialDownloadEvent(material, findUser(authenticatedUser.getId())));
    }

    private StudyMaterialFolder resolveFolder(UUID folderId) {
        return folderId == null ? null : findFolder(folderId);
    }

    private StudyMaterialFolder findFolder(UUID folderId) {
        return folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Study material folder was not found"));
    }

    private StudyMaterial findMaterial(UUID materialId) {
        return materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Study material was not found"));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));
    }

    private void ensureUniqueFolderName(String name, UUID parentId, UUID excludeId) {
        if (folderRepository.existsSiblingWithName(name, parentId, excludeId)) {
            throw new IllegalArgumentException("A folder with this name already exists at this level");
        }
    }

    private void validateFile(MultipartFile file, MaterialType materialType) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Study material file is required");
        }
        if (file.getSize() > storageProperties.getMaxFileSizeBytes()) {
            throw new IllegalArgumentException("Study material file exceeds the configured maximum size");
        }
        String contentType = normalizeContentType(file.getContentType());
        boolean valid = switch (materialType) {
            case PDF -> PDF_TYPES.contains(contentType);
            case PPT -> PPT_TYPES.contains(contentType);
            case DOCX -> DOCX_TYPES.contains(contentType);
            default -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException("Study material file type does not match the selected material type");
        }
    }

    private void validateFileMaterialType(MaterialType materialType) {
        if (materialType == null || !(MaterialType.PDF.equals(materialType)
                || MaterialType.PPT.equals(materialType)
                || MaterialType.DOCX.equals(materialType))) {
            throw new IllegalArgumentException("File material type must be PDF, PPT, or DOCX");
        }
    }

    private void validateLinkMaterialType(MaterialType materialType) {
        if (materialType == null || !(MaterialType.VIDEO_LINK.equals(materialType)
                || MaterialType.EXTERNAL_LINK.equals(materialType))) {
            throw new IllegalArgumentException("Link material type must be VIDEO_LINK or EXTERNAL_LINK");
        }
    }

    private String normalizeRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeSearch(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeUrl(String value) {
        String url = normalizeRequired(value, "External URL is required");
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            throw new IllegalArgumentException("External URL must start with http:// or https://");
        }
        return url;
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
    }

    private Pageable normalizeFolderPageable(Pageable pageable) {
        return normalizePageable(pageable, Set.of("id", "name", "createdAt", "updatedAt", "createdAtUtc", "updatedAtUtc"));
    }

    private Pageable normalizeMaterialPageable(Pageable pageable) {
        return normalizePageable(pageable, Set.of(
                "id",
                "title",
                "materialType",
                "sourceType",
                "downloadCount",
                "createdAt",
                "updatedAt",
                "createdAtUtc",
                "updatedAtUtc"
        ));
    }

    private Pageable normalizePageable(Pageable pageable, Set<String> allowedProperties) {
        if (pageable.isUnpaged()) {
            return pageable;
        }
        Sort sort = Sort.by(pageable.getSort().stream()
                .map(order -> toRepositorySortOrder(order, allowedProperties))
                .toList());
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private Sort.Order toRepositorySortOrder(Sort.Order order, Set<String> allowedProperties) {
        if (!allowedProperties.contains(order.getProperty())) {
            throw new IllegalArgumentException("Unsupported sort property: " + order.getProperty());
        }
        String property = switch (order.getProperty()) {
            case "createdAtUtc" -> "createdAt";
            case "updatedAtUtc" -> "updatedAt";
            default -> order.getProperty();
        };
        Sort.Order translated = new Sort.Order(order.getDirection(), property, order.getNullHandling());
        return order.isIgnoreCase() ? translated.ignoreCase() : translated;
    }
}

