package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.utils;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.aperteworkflow.files.IFilesRepositoryFacade;
import org.aperteworkflow.files.exceptions.DownloadFileException;
import org.aperteworkflow.files.model.FileItemContent;
import org.aperteworkflow.files.model.IFilesRepositoryItem;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.UserDataBean;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.dao.BpmNotificationMailPropertiesDAO;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmAttachment;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationMailProperties;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.EmailSender;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.IBpmNotificationService;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.NotificationData;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateData;

import java.util.*;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;
import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * Created by pkuciapski on 2014-07-25.
 */
public class EmailUtils {
    public static void sendMail(String template, IAttributesProvider attributesProvider, String recipient, String templateArgumentProvider, String profileName,
                                IFilesRepositoryFacade filesRepository, List<Long> attachmentIds, String source, boolean allAttachments) throws Exception {
        IBpmNotificationService service = getRegistry().getRegisteredService(IBpmNotificationService.class);
        TemplateData templateData =	service.createTemplateData(template, Locale.getDefault());
        UserData user = getRecipient(recipient);

        service.getTemplateDataProvider()
                .addProcessData(templateData, attributesProvider)
                .addUserToNotifyData(templateData, user)
                .addArgumentProvidersData(templateData, templateArgumentProvider, attributesProvider);

        NotificationData notificationData = new NotificationData()
                .setProfileName(profileName)
                .setRecipient(user)
                .setTemplateData(templateData);

        notificationData.setAttachments(getAttachments(attributesProvider, attachmentIds, filesRepository, allAttachments));

        if (hasText(source)) {
            notificationData.setSource(source);
        }
        else {
            notificationData.setSource(String.valueOf(attributesProvider.getId()));
        }

        notificationData.setDefaultSender(getDefaultSender(profileName));

        EmailSender.sendEmail(service, notificationData);

    }

    public static UserData getRecipient(String recipient) {
        if (recipient.contains("@")) {
            UserDataBean result = new UserDataBean();
            result.setEmail(recipient);
            return result;
        }
        return getRegistry().getUserSource().getUserByLogin(recipient);
    }

    public static List<BpmAttachment> getAttachments(IAttributesProvider provider, List<Long> attachmentIds, IFilesRepositoryFacade filesRepository, boolean allAttachments) {
        if (attachmentIds == null || (attachmentIds != null && attachmentIds.size() == 0)) {
            return Collections.emptyList();
        }

        List<BpmAttachment> result = new ArrayList<BpmAttachment>();

        if (allAttachments) {
            for (IFilesRepositoryItem repositoryItem : filesRepository.getFilesList(provider)) {
                result.add(getBpmAttachment(repositoryItem.getId(), filesRepository));
            }
        }
        else {
            for (Long attachmentId : attachmentIds) {
                result.add(getBpmAttachment(attachmentId, filesRepository));
            }
        }
        return result;
    }

    private static BpmAttachment getBpmAttachment(Long attachmentId, IFilesRepositoryFacade filesRepository) {
        try {
            FileItemContent fileItemContent = filesRepository.downloadFile(attachmentId);
            BpmAttachment attachment = new BpmAttachment();

            attachment.setName(fileItemContent.getName());
            attachment.setContentType(fileItemContent.getContentType());
            attachment.setBody(fileItemContent.getBytes());
            return attachment;
        }
        catch (DownloadFileException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getDefaultSender(String profileName) {
        BpmNotificationMailProperties profile = new BpmNotificationMailPropertiesDAO().getProfile(profileName);
        if(profile == null)
            throw new RuntimeException("There is no profile in database with name: "+profileName);
        return profile.getDefaultSender();
    }

    public static List<Long> getAttachmentIds(String parameter) {
        if (parameter != null) {
            List<String> list = Arrays.asList(parameter.split(","));
            return Lists.transform(list, new Function<String, Long>() {
                @Override
                public Long apply(String s) {
                    return Long.valueOf(s);
                }
            });
        } else
            return null;
    }
}
