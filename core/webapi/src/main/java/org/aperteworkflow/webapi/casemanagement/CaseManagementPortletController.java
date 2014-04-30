package org.aperteworkflow.webapi.casemanagement;

import org.aperteworkflow.webapi.PortletUtil;
import org.aperteworkflow.webapi.main.DispatcherController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;
import pl.net.bluesoft.rnd.processtool.ISettingsProvider;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;

import javax.portlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pkuciapski on 2014-04-30.
 */
@Controller(value = "CaseManagementPortletController")
@RequestMapping("VIEW")
public class CaseManagementPortletController {

    private static final String PORTLET_JSON_RESULT_ROOT_NAME = "result";

    private static Logger logger = Logger.getLogger(CaseManagementPortletController.class.getName());

    @Autowired(required = false)
    protected IPortalUserSource portalUserSource;

    @Autowired(required = false)
    protected ISettingsProvider settingsProvider;

    @Autowired(required = false)
    private DispatcherController mainDispatcher;


    @RenderMapping()
    /**
     * main view handler for Portlet.
     */
    public ModelAndView handleMainRenderRequest(RenderRequest request, RenderResponse response, Model model) {
        logger.info("CaseManagementPortletController.handleMainRenderRequest... ");
        PortletConfig config = ((PortletConfig) request.getAttribute("javax.portlet.config"));

        if (config == null)
            throw new RuntimeException("No portlet config!");

        String viewName = config.getInitParameter("view-jsp");
        if (viewName == null)
            throw new RuntimeException("There is no view-jsp inintial paramter set for this portlet");


        ModelAndView modelView = new ModelAndView();
        modelView.setView(viewName);

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
            logger.log(Level.SEVERE, "[ERROR] No controller parameter in dispatcher invocation!");
            throw new PortletException("No controller parameter!");
        } else if (action == null || action.isEmpty()) {
            logger.log(Level.SEVERE, "[ERROR] No action parameter in dispatcher invocation!");
            throw new PortletException("No action parameter!");
        } else {
            HttpServletResponse httpServletResponse = portalUserSource.getHttpServletResponse(response);
            return PortletUtil.translate(PORTLET_JSON_RESULT_ROOT_NAME,
                    mainDispatcher.invokeExternalController(controller, action, originalHttpServletRequest, httpServletResponse));
        }
    }

}

