package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.facade;

import java.util.*;

import org.hibernate.CacheMode;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotification;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationMailProperties;

/**
 * Facade layer for the Notification database access
 * 
 * @author Maciej Pawlak
 *
 */
public class NotificationsFacade 
{
	/** Get all notifications waiting to be sent */
	public static Collection<BpmNotification> getNotificationsToSend(int interval)
	{
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        int time = 1000*(c.get(Calendar.HOUR_OF_DAY) * 3600 + c.get(Calendar.MINUTE) * 60 + c.get(Calendar.SECOND));

        return (List<BpmNotification>)getSession()
                .createQuery("from BpmNotification as n " +
                        "where n.groupNotifications=false or (n.groupNotifications=true and n.sendAfterHour>=:from and n.sendAfterHour<=:to) " +
                        "order by recipient asc")
                .setParameter("from", time-interval)
                .setParameter("to", time+interval)
                .setLockOptions(LockOptions.NONE)
                .setCacheMode(CacheMode.IGNORE)
                .list();
	}

	/** Get all notifications properties */
	public static Collection<BpmNotificationMailProperties> getNotificationMailProperties()
	{
		Session session = getSession();

		return session.createCriteria(BpmNotificationMailProperties.class).list();
	}

	/** Saves given notifications to database */
	public static void addNotificationToBeSent(BpmNotification notification)
	{
		Session session = getSession();
		
		session.saveOrUpdate(notification);
	}
	
	public static void removeNotification(BpmNotification notification) 
	{
		Session session = getSession();
		
		session.delete(notification);
	}
	
	private static Session getSession()
	{
		return ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession();
	}



}
