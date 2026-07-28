package com.company.learninghub.assistant.tool;

import com.company.learninghub.assistant.intent.AssistantIntentType;
import com.company.learninghub.assistant.intent.ResolvedIntent;
import com.company.learninghub.profile.dto.ProfileResponse;
import com.company.learninghub.profile.service.ProfileService;
import org.springframework.stereotype.Component;

@Component
public class MyProfileTool implements AssistantTool {

    private final ProfileService profileService;

    public MyProfileTool(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Override
    public String getName() {
        return AssistantToolNames.MY_PROFILE;
    }

    @Override
    public boolean supports(ResolvedIntent intent) {
        return intent.type() == AssistantIntentType.TOOL
                && AssistantToolNames.MY_PROFILE.equals(intent.toolName());
    }

    @Override
    public ToolResult execute(AssistantToolContext context) {
        ProfileResponse profile = profileService.getProfile(context.authenticatedUser());
        String text = "Profile for %s (%s).".formatted(profile.fullName(), profile.email());
        return ToolResult.structured(text, profile);
    }
}
