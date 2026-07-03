package com.company.learninghub.learn.controller;

import com.company.learninghub.auth.security.AuthenticatedUser;
import com.company.learninghub.common.pagination.PageResponse;
import com.company.learninghub.learn.domain.TechnologyCategory;
import com.company.learninghub.learn.domain.TechnologyDifficulty;
import com.company.learninghub.learn.dto.TechnologyResponse;
import com.company.learninghub.learn.service.LearnTechnologyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learn/technologies")
@Tag(name = "Learn Technologies", description = "Browse and view published technologies")
@SecurityRequirement(name = "bearerAuth")
public class LearnTechnologyController {

    private final LearnTechnologyService technologyService;

    public LearnTechnologyController(LearnTechnologyService technologyService) {
        this.technologyService = technologyService;
    }

    @GetMapping
    @Operation(
            summary = "List published technologies",
            description = """
                    Returns paginated published technologies for authenticated users.
                    Supports whole-term, case-insensitive search across name, short name, slug,
                    tags, and description with relevance ranking, plus category and difficulty filters.
                    """
    )
    public ResponseEntity<PageResponse<TechnologyResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TechnologyCategory category,
            @RequestParam(required = false) TechnologyDifficulty difficulty,
            @Parameter(
                    description = "Pagination and sorting. Supported sort fields include name, category, difficulty, featured, createdAtUtc, and updatedAtUtc."
            )
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponse.from(
                technologyService.listEmployeeTechnologies(search, category, difficulty, pageable)
        ));
    }

    @GetMapping("/{technologyId}")
    @Operation(
            summary = "View a technology",
            description = "Admins can view all technologies. Employees can view only published technologies."
    )
    public ResponseEntity<TechnologyResponse> getById(
            @PathVariable UUID technologyId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(technologyService.getById(technologyId, authenticatedUser));
    }
}
