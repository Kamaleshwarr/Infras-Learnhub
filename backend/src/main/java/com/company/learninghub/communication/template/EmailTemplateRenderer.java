package com.company.learninghub.communication.template;

import com.company.learninghub.communication.config.CommunicationProperties;
import com.company.learninghub.communication.domain.CommunicationEvent;
import com.company.learninghub.communication.email.EmailMessage;
import com.company.learninghub.user.domain.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
public class EmailTemplateRenderer {

    private final SpringTemplateEngine htmlTemplateEngine;
    private final SpringTemplateEngine textTemplateEngine;
    private final EmailTemplateVariables templateVariables;
    private final CommunicationProperties communicationProperties;

    public EmailTemplateRenderer(
            @Qualifier("communicationHtmlTemplateEngine") SpringTemplateEngine htmlTemplateEngine,
            @Qualifier("communicationTextTemplateEngine") SpringTemplateEngine textTemplateEngine,
            EmailTemplateVariables templateVariables,
            CommunicationProperties communicationProperties
    ) {
        this.htmlTemplateEngine = htmlTemplateEngine;
        this.textTemplateEngine = textTemplateEngine;
        this.templateVariables = templateVariables;
        this.communicationProperties = communicationProperties;
    }

    public RenderedEmail render(User recipient, CommunicationEvent event) {
        EmailTemplateModel model = templateVariables.build(recipient, event);
        return render(model);
    }

    public RenderedEmail render(EmailTemplateModel model) {
        Context context = toContext(model);
        String htmlBody = htmlTemplateEngine.process("email/" + model.templateName(), context);
        String textBody = textTemplateEngine.process("email/" + model.templateName(), context);
        return new RenderedEmail(model.templateName(), model.subject(), htmlBody, textBody);
    }

    public String renderSubject(EmailTemplateModel model) {
        return model.subject();
    }

    public String renderHtml(EmailTemplateModel model) {
        return htmlTemplateEngine.process("email/" + model.templateName(), toContext(model));
    }

    public String renderText(EmailTemplateModel model) {
        return textTemplateEngine.process("email/" + model.templateName(), toContext(model));
    }

    public EmailMessage buildEmailMessage(User recipient, CommunicationEvent event) {
        RenderedEmail rendered = render(recipient, event);
        return new EmailMessage(
                communicationProperties.getEmail().getFrom(),
                recipient.getEmail(),
                rendered.subject(),
                rendered.textBody(),
                rendered.htmlBody()
        );
    }

    private Context toContext(EmailTemplateModel model) {
        Context context = new Context();
        context.setVariable("model", model);
        context.setVariable("recipientName", model.recipientName());
        context.setVariable("recipientEmail", model.recipientEmail());
        context.setVariable("actorName", model.actorName());
        context.setVariable("message", model.message());
        context.setVariable("actionUrl", model.actionUrl());
        context.setVariable("actionLabel", model.actionLabel());
        context.setVariable("applicationUrl", model.applicationUrl());
        context.setVariable("supportEmail", model.supportEmail());
        context.setVariable("currentYear", model.currentYear());
        context.setVariable("projectName", model.projectName());
        context.setVariable("certificationName", model.certificationName());
        context.setVariable("technologyName", model.technologyName());
        context.setVariable("resetUrl", model.resetUrl());
        context.setVariable("expirationMinutes", model.expirationMinutes());
        context.setVariable("priority", model.priority());
        model.extraVariables().forEach(context::setVariable);
        return context;
    }
}
