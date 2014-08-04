package org.aperteworkflow.files.dao;

import org.aperteworkflow.files.model.*;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;

import java.util.*;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class FilesRepositoryItemDAOImpl extends SimpleHibernateBean<IFilesRepositoryAttribute> implements FilesRepositoryItemDAO {

    public FilesRepositoryItemDAOImpl(Session session) {
        super(session);
    }

    @Override
    public FilesRepositoryItem addItem(IAttributesConsumer consumer, String name, String description, String relativePath, String contentType, String creatorLogin, FilesRepositoryAttributeFactory factory) {
        return addItem(consumer, name, description, relativePath, contentType, creatorLogin, false, factory);
    }

    @Override
    public FilesRepositoryItem addItem(IAttributesConsumer consumer, String name, String description, String relativePath, String contentType, String creatorLogin, Boolean sendAsEmail, FilesRepositoryAttributeFactory factory) {
        FilesRepositoryItem item = new FilesRepositoryItem();
        item.setName(name);
        item.setDescription(description);
        item.setRelativePath(relativePath);
        item.setContentType(contentType);
        item.setCreateDate(new Date());
        item.setCreatorLogin(creatorLogin);
        IFilesRepositoryAttribute attribute = (IFilesRepositoryAttribute) consumer.getAttribute(FilesRepositoryAttributes.FILES.value());
        if (attribute == null) {
            attribute = factory.create();
            attribute.setKey(FilesRepositoryAttributes.FILES.value());
            consumer.setAttribute(FilesRepositoryAttributes.FILES.value(), attribute);
        }
        attribute.getFilesRepositoryItems().add(item);
        getSession().saveOrUpdate(item);
        return item;
    }

    @Override
    public Collection<FilesRepositoryItem> getItemsFor(IAttributesProvider provider) {
        IFilesRepositoryAttribute filesAttribute = (IFilesRepositoryAttribute) provider.getAttribute(FilesRepositoryAttributes.FILES.value());
        if (filesAttribute == null)
            return new HashSet<FilesRepositoryItem>();
        return filesAttribute.getFilesRepositoryItems();
    }

    @Override
    public void deleteById(IAttributesProvider provider, Long itemId) {
        FilesRepositoryItem item = getItemById(itemId);
        IFilesRepositoryAttribute filesAttribute = (IFilesRepositoryAttribute) provider.getAttribute(FilesRepositoryAttributes.FILES.value());
        filesAttribute.removeItem(item);
        getSession().delete(item);
    }

    @Override
    public void updateDescription(IFilesRepositoryItem item, String description) {
        item.setDescription(description);
        getSession().saveOrUpdate(item);
    }

    @Override
    public FilesRepositoryItem getItemById(Long id) {
        return (FilesRepositoryItem) getSession().get(FilesRepositoryItem.class, id);
    }

    @Override
    public void updateSendWithMail(IFilesRepositoryItem item, Boolean sendWithMail) {
        item.setSendWithMail(sendWithMail);
        getSession().saveOrUpdate(item);
    }

}
