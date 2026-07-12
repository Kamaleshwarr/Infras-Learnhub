package com.company.learninghub.communication.domain;

/**
 * Priority for communication delivery ordering.
 * Version 1 uses this for outbox poll ordering only; no other runtime behavior differs by priority.
 */
public enum CommunicationPriority {
    HIGH,
    NORMAL,
    LOW
}
