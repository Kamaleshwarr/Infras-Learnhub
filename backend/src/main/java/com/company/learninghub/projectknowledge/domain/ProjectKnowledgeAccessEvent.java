package com.company.learninghub.projectknowledge.domain;

import com.company.learninghub.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "project_knowledge_access_events")
public class ProjectKnowledgeAccessEvent {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private ProjectKnowledgeItem item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accessed_by", nullable = false)
    private User accessedBy;

    @Column(name = "accessed_at_utc", nullable = false, updatable = false)
    private Instant accessedAtUtc;

    protected ProjectKnowledgeAccessEvent() {
    }

    public ProjectKnowledgeAccessEvent(ProjectKnowledgeItem item, User accessedBy) {
        this.item = item;
        this.accessedBy = accessedBy;
    }

    @PrePersist
    protected void onCreate() {
        this.accessedAtUtc = Instant.now();
    }
}

