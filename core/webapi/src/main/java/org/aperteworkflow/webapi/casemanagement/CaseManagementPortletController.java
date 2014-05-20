package org.aperteworkflow.webapi.casemanagement;

import org.aperteworkflow.webapi.PortletUtil;
import org.aperteworkflow.webapi.main.DispatcherController;
import org.aperteworkflow.webapi.tools.WebApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;

import javax.portlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller(value = "CaseManagementPortletController")
@RequestMapping("VIEW")
/**
 * Portal portlet main controller class. In case of calling servlet request, use portlet resource
 * mapping to obtain portal specific attributes and cookies
 */
public class CaseManagementPortletController {
    protected static final String PORTLET_JSON_RESULT_ROOT_NAME = "result";
    protected static final String PORTLET_CASE_ID_PARAMTER = "caseId";

    private static Logger logger = Logger.getLogger(CaseManagementPortletController.class.getName());

    @Autowired(required = false)
    private DispatcherController mainDispatcher;

    @Autowired(required = false)
    protected IPortalUserSource portalUserSource;


    @RenderMapping()
    /**
     * main view handler for Portlet.
     */
    public ModelAndView handleMainRenderRequest(RenderRequest request, RenderResponse response, Model model) {
        logger.info("CaseManagementPortletController.handleMainRenderRequest... ");
        ModelAndView modelView = new ModelAndView();
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        UserData user = portalUserSource.getUserByRequest(request);
        modelView.addObject(WebApiConstants.USER_PARAMETER_NAME, user);
        if (user == null || user.getLogin() == null) {
            modelView.setViewName("login");
        } else {
            modelView.setViewName("case_list");
        }

        HttpServletRequest httpServletRequest = portalUserSource.getHttpServletRequest(request);
        HttpServletRequest originalHttpServletRequest = portalUserSource.getOriginalHttpServletRequest(httpServletRequest);

        /* Start from case view */
        String showCaseId = originalHttpServletRequest.getParameter(PORTLET_CASE_ID_PARAMTER);
        if (showCaseId != null) {
            modelView.addObject(PORTLET_CASE_ID_PARAMTER, showCaseId);
        }

        return modelView;
    }


    @ResourceMapping("dispatcher")
    @ResponseBody
    public ModelAndView dispatcher(ResourceRequest request, ResourceResponse response) throws PortletException {
        HttpServletRequest originalHttpServletRequest = PortletUtil.getOriginalHttpServletRequest(portalUserSource, request);

        String controller = originalHttpServletRequest.getParameter("controller");
        String action = originalHttpServletRequest.getParameter("action");

        logger.log(Level.INFO, "controllerName: " + controller + ", action: " + action);

        if (controller == null || controller.isEmpty()) {
            logger.log(Level.SEVERE, "[ERROR] No controller paramter in dispatcher invocation!");
            throw new PortletException("No controller paramter!");
        } else if (action == null || action.isEmpty()) {
            logger.log(Level.SEVERE, "[ERROR] No action paramter in dispatcher invocation!");
            throw new PortletException("No action paramter!");
        }

        HttpServletResponse httpServletResponse = getHttpServletResponse(response);

        return PortletUtil.translate(PORTLET_JSON_RESULT_ROOT_NAME,
                mainDispatcher.invokeExternalController(controller, action, originalHttpServletRequest, httpServletResponse));
    }

    @ResourceMapping("noReplyDispatcher")
    @ResponseBody
    public void noReplyDispatcher(ResourceRequest request, ResourceResponse response) throws PortletException {
        HttpServletRequest originalHttpServletRequest = PortletUtil.getOriginalHttpServletRequest(portalUserSource, request);

        String controller = originalHttpServletRequest.getParameter("controller");
        String action = originalHttpServletRequest.getParameter("action");

        logger.log(Level.INFO, "fileDispatcher: controllerName: " + controller + ", action: " + action);

        if (controller == null || controller.isEmpty()) {
            logger.log(Level.SEVERE, "[ERROR] fileDispatcher: No controller paramter in dispatcher invocation!");
            throw new PortletException("No controller paramter!");
        } else if (action == null || action.isEmpty()) {
            logger.log(Level.SEVERE, "[ERROR] fileDispatcher: No action paramter in dispatcher invocation!");
            throw new PortletException("No action paramter!");
        } else {
            HttpServletResponse httpServletResponse = getHttpServletResponse(response);
            mainDispatcher.invokeExternalController(controller, action, originalHttpServletRequest, httpServletResponse);
        }
    }

    @ResourceMapping("fileUploadDispatcher")
    @ResponseBody
    public ModelAndView fileUploadDispatcher(ResourceRequest request, ResourceResponse response) throws PortletException {
        // IE doesnt properly handles application/json content type in response when uploading file.
        HttpServletRequest originalHttpServletRequest = PortletUtil.getOriginalHttpServletRequest(portalUserSource, request);
        HttpServletResponse httpServletResponse = getHttpServletResponse(response);
        if (originalHttpServletRequest.getHeader("HTTP_ACCEPT") != null
                && originalHttpServletRequest.getHeader("HTTP_ACCEPT").indexOf("application/json") > -1) {
            httpServletResponse.setContentType("application/json");
        } else {
            httpServletResponse.setContentType("text/plain");
        }
        return dispatcher(request, response);
    }

    /**
     * Obtain http servlet response from ajax request
     */
    private HttpServletResponse getHttpServletResponse(ResourceResponse response) {
        try {
            return portalUserSource.getHttpServletResponse(response);
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "[PORTLET CONTROLLER] Error", ex);
            throw new RuntimeException(ex);
        }
    }

}