package com.company.learninghub.communication.service;

import com.company.learninghub.communication.channel.EmailChannelHandler;
import com.company.learninghub.communication.channel.InAppChannelHandler;
import com.company.learninghub.communication.domain.CommunicationChannel;
import com.company.learninghub.communication.domain.CommunicationEvent;
import com.company.learninghub.user.domain.User;
import com.company.learninghub.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CommunicationDispatcher {

    private final InAppChannelHandler inAppChannelHandler;
    private final EmailChannelHandler emailChannelHandler;
    private final UserRepository userRepository;

    public CommunicationDispatcher(
            InAppChannelHandler inAppChannelHandler,
            EmailChannelHandler emailChannelHandler,
            UserRepository userRepository
    ) {
        this.inAppChannelHandler = inAppChannelHandler;
        this.emailChannelHandler = emailChannelHandler;
        this.userRepository = userRepository;
    }

    public void dispatch(CommunicationEvent event) {
        if (event.channels().isEmpty()) {
            throw new IllegalArgumentException("At least one communication channel is required");
        }
        if (!StringUtils.hasText(event.idempotencyKey())) {
            throw new IllegalArgumentException("Communication idempotency key is required");
        }

        User recipient = userRepository.findById(event.recipientUserId()).orElse(null);
        if (recipient == null) {
            return;
        }

        if (event.channels().contains(CommunicationChannel.IN_APP)) {
            if (!recipient.isActive()) {
                return;
            }
            inAppChannelHandler.deliver(event, recipient);
        }

        if (event.channels().contains(CommunicationChannel.EMAIL)) {
            if (!recipient.isActive() || !StringUtils.hasText(recipient.getEmail())) {
                return;
            }
            emailChannelHandler.enqueue(event);
        }
    }
}
