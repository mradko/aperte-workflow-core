package org.aperteworkflow.webapi.experm;

import org.aperteworkflow.webapi.main.AbstractMainController;
import org.aperteworkflow.webapi.main.MainDispatcher;
import org.aperteworkflow.webapi.main.processes.controller.ProcessesListController;
import org.aperteworkflow.webapi.main.processes.controller.TaskViewController;
import org.aperteworkflow.webapi.main.queues.controller.QueuesController;
import org.aperteworkflow.webapi.main.util.MappingJacksonJsonViewEx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;

import javax.portlet.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller(value = "ExPermissionViewController")
@RequestMapping("VIEW")
/**
 * Portal portlet main controller class. In case of calling servlet request, use portlet resource
 * mapping to obtain portal specific attributes and cookies
 */
public class ExPermissionViewController

{
    private static final String PORTLET_JSON_RESULT_ROOT_NAME = "result";

    private static Logger logger = Logger.getLogger(ExPermissionViewController.class.getName());



    @Autowired(required = false)
    private MainDispatcher mainDispatcher;

    @Autowired(required = false)
    protected IPortalUserSource portalUserSource;

	@RenderMapping ()
	public ModelAndView handleMainRenderRequest(RenderRequest request, RenderResponse response, Model model)
    {
		logger.info("ExPermissionViewController.handleMainRenderRequest... ");
		
        ModelAndView modelView = new ModelAndView();
        modelView.setViewName("ex_permission");




        return modelView;
    }





    @ResourceMapping( "dispatcher")
    @ResponseBody
    public ModelAndView dispatcher(ResourceRequest request, ResourceResponse response) throws PortletException
    {
        HttpServletRequest originalHttpServletRequest = getOriginalHttpServletRequest(request);

        String controller = originalHttpServletRequest.getParameter("controller");
        String action = originalHttpServletRequest.getParameter("action");

        logger.log(Level.INFO, "controllerName: "+controller+", action: "+action);

        if(controller == null || controller.isEmpty())
        {
            logger.log(Level.SEVERE, "[ERROR] No controller paramter in dispatcher invocation!");
            throw new PortletException("No controller paramter!");
        }
        else if(action == null || action.isEmpty())
        {
            logger.log(Level.SEVERE, "[ERROR] No action paramter in dispatcher invocation!");
            throw new PortletException("No action paramter!");
        }


        return  translate(PORTLET_JSON_RESULT_ROOT_NAME,
                mainDispatcher.invokeExternalController(controller, action, originalHttpServletRequest));
    }

    @ResourceMapping( "getData")
    @ResponseBody
    public ModelAndView getUserQueues(ResourceRequest request, ResourceResponse response)
    {
        return  translate(PORTLET_JSON_RESULT_ROOT_NAME, new String("test"));
    }


    /** Obtain http servlet request with additional attributes from ajax request */
    private HttpServletRequest getOriginalHttpServletRequest(ResourceRequest request)
    {
        try {
            HttpServletRequest httpServletRequest = portalUserSource.getHttpServletRequest(request);
            HttpServletRequest originalHttpServletRequest =  portalUserSource.getOriginalHttpServletRequest(httpServletRequest);

            /* Copy all attributes, because portlet attributes do not exist in original request */
            originalHttpServletRequest.getParameterMap().putAll(httpServletRequest.getParameterMap());

            return  originalHttpServletRequest;
        }
        catch(Throwable ex)
        {
            logger.log(Level.SEVERE, "[PORTLET CONTROLLER] Error", ex);
            throw new RuntimeException(ex);
        }

    }

    /** Translate DTO object to json in model and view, which is required for portlet resource serving */
    private ModelAndView translate(String resultName, Object result)
    {
        ModelAndView mav = new ModelAndView();
        MappingJacksonJsonViewEx v = new MappingJacksonJsonViewEx();
        v.setBeanName(resultName);

        mav.setView(v);
        mav.addObject(resultName, result);

        return mav;
    }

}