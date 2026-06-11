package com.company.learninghub.auth.exception;

import org.springframework.security.access.AccessDeniedException;

public class PasswordChangeRequiredException extends AccessDeniedException {

    public PasswordChangeRequiredException() {
        super("Password change required before accessing this resource");
    }
}
