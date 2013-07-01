package pl.net.bluesoft.rnd.pt.ext.jbpm;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.jbpm.service.JbpmService;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

/**
 * Process Tool Context factory
 * 
 * @author tlipski@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
public class ProcessToolContextFactoryImpl implements ProcessToolContextFactory, ProcessToolBpmConstants {
    private static Logger logger = Logger.getLogger(ProcessToolContextFactoryImpl.class.getName());
    private ProcessToolRegistry registry;

    public ProcessToolContextFactoryImpl(ProcessToolRegistry registry) {
        this.registry = registry;
        initJbpmConfiguration();
    }

    @Override
    public <T> T withExistingOrNewContext(ReturningProcessToolContextCallback<T> callback) 
    {
    	return withProcessToolContext(callback);
    }

    @Override
	public <T> T withProcessToolContext(ReturningProcessToolContextCallback<T> callback) 
    {
    	ProcessToolContext ctx = getThreadProcessToolContext();
    	/* Active context already exists, use it */
    	if(ctx != null && ctx.isActive())
    		return callback.processWithContext(ctx);
    	
    	/* Context is set but its session is closed, remove it */
    	if(ctx != null && !ctx.isActive())
    		ProcessToolContext.Util.removeThreadProcessToolContext();
    	
    	ProcessToolRegistry.ThreadUtil.setThreadRegistry(registry);
    	
        if (registry.isJta()) {
            return withProcessToolContextJta(callback);
        } else {
            return withProcessToolContextNonJta(callback);
        }
    }

	public <T> T withProcessToolContextNonJta(ReturningProcessToolContextCallback<T> callback) 
	{
        T result = null;

		Session session = registry.getSessionFactory().openSession();
		try 
		{
			try 
			{
				Transaction tx = session.beginTransaction();
				ProcessToolContext ctx = new ProcessToolContextImpl(session, registry);
				ProcessToolContext.Util.setThreadProcessToolContext(ctx);
				try 
				{
					result = callback.processWithContext(ctx);
				} 
				catch (RuntimeException e) 
				{
					logger.log(Level.SEVERE, e.getMessage(), e);
					try {
						tx.rollback();
						ctx.rollback();
					} catch (Exception e1) {
						logger.log(Level.WARNING, e1.getMessage(), e1);
					}
					throw e;
				}
				finally
				{
					ctx.close();
					ProcessToolContext.Util.removeThreadProcessToolContext();
				}
				tx.commit();
			}
			finally {
//				TODO pi.close();
			}
		} 
		finally 
		{
			session.close();
		}
        return result;
    }

    public <T> T withProcessToolContextJta(ReturningProcessToolContextCallback<T> callback) {
        T result = null;

        try {
			UserTransaction ut = getUserTransaction();

            logger.fine("ut.getStatus() = " + ut.getStatus());

            if (ut.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                ut.rollback();
            }
            if (ut.getStatus() != Status.STATUS_ACTIVE) {
				ut.begin();
			}

			Session session = registry.getSessionFactory().getCurrentSession();

			try {
                try 
                {
                	ProcessToolContext ctx = new ProcessToolContextImpl(session, registry);
                	ProcessToolContext.Util.setThreadProcessToolContext(ctx);
                    try 
                    {
                        result = callback.processWithContext(ctx);
                    } 
                    catch (Exception e) {
                        logger.log(Level.SEVERE, e.getMessage(), e);
                        try 
                        {
                            ut.rollback();
        					ctx.rollback();
        					
                        } catch (Exception e1) {
                            logger.log(Level.WARNING, e1.getMessage(), e1);
                        }
                        throw e;
                    }
                    finally
                    {
                    	ctx.close();
                    	ProcessToolContext.Util.removeThreadProcessToolContext();
                    }
                } finally {
//TODO                    pi.close();
                }
            } finally {
                session.flush();
            }
            ut.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;


    }

	private UserTransaction getUserTransaction() throws NamingException {
		UserTransaction ut;
		try {
			ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
		}
		catch (Exception e) {
			//it should work on jboss regardless. But it does not..
			logger.warning("java:comp/UserTransaction not found, looking for UserTransaction");
			ut = (UserTransaction) new InitialContext().lookup("UserTransaction");
		}
		return ut;
	}

//	private ProcessEngine getProcessEngine() {
//        Thread t = Thread.currentThread();
//        ClassLoader previousLoader = t.getContextClassLoader();
//        try {
//            ClassLoader newClassLoader = configuration.getClass().getClassLoader();
//            t.setContextClassLoader(newClassLoader);
//            return configuration.buildProcessEngine();
//        } finally {
//            t.setContextClassLoader(previousLoader);
//        }
//    }

    @Override
    public ProcessToolRegistry getRegistry() {
        return registry;
    }

    @Override
	public void updateSessionFactory(SessionFactory sf) {
//        if (configuration != null) {
//            configuration.setHibernateSessionFactory(sf);
//        }
    }

    public void initJbpmConfiguration() {
		JbpmService.getInstance().init();

//        Thread t = Thread.currentThread();
//        ClassLoader previousLoader = t.getContextClassLoader();
//        try {
//            ClassLoader newClassLoader = getClass().getClassLoader();
//            t.setContextClassLoader(newClassLoader);
//            configuration = new Configuration();
//            configuration.setHibernateSessionFactory(registry.getSessionFactory());
//        } finally {
//            t.setContextClassLoader(previousLoader);
//        }
    }

}
