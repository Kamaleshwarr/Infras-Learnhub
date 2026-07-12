package com.company.learninghub.communication.domain;

public enum CommunicationOutboxStatus {
    PENDING,
    PROCESSING,
    SENT,
    FAILED,
    DEAD
}
