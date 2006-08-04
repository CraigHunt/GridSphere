/*
 * @author <a href="mailto:wehrens@gridsphere.org">Oliver Wehrens</a>
 * @version $Id: TitleBar.java 4496 2006-02-08 20:27:04Z wehrens $
 */
package org.gridlab.gridsphere.layout.view.brush;

import org.gridlab.gridsphere.layout.PortletComponent;
import org.gridlab.gridsphere.layout.PortletTitleBar;
import org.gridlab.gridsphere.layout.view.BaseRender;
import org.gridlab.gridsphere.layout.view.Render;
import org.gridlab.gridsphere.portlet.PortletRequest;
import org.gridlab.gridsphere.portlet.impl.SportletProperties;
import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;

import java.util.Iterator;
import java.util.List;

public class TitleBar extends BaseRender implements Render {

    /**
     * Constructs an instance of PortletTitleBar
     */
    public TitleBar() {
    }

    public StringBuffer doStart(GridSphereEvent event, PortletComponent comp) {
        PortletTitleBar titleBar = (PortletTitleBar) comp;
        StringBuffer titleBuffer = new StringBuffer();
        // if (titleBar.isActive()) {
        titleBuffer.append("<div class=\"gridsphere-window-title\">");
        titleBar.setActive(false);
        titleBuffer.append("<div class=\"gridsphere-window-title-name\">&nbsp;"); // add a space to not have it to much leftish
        return titleBuffer;
    }

    public StringBuffer doEnd(GridSphereEvent event, PortletComponent comp) {
        PortletTitleBar titleBar = (PortletTitleBar) comp;
        PortletRequest req = event.getPortletRequest();
        StringBuffer titleBuffer = new StringBuffer();
        titleBuffer.append("</div>"); // close window title name
        // Output window state icons
        List windowLinks = titleBar.getWindowLinks();
        titleBuffer.append("<div class=\"gridsphere-window-title-icon-right\">");
        List modeLinks = titleBar.getModeLinks();
        String renderKit = (String)req.getPortletSession().getAttribute(SportletProperties.LAYOUT_RENDERKIT);
        String theme = (String)req.getPortletSession().getAttribute(SportletProperties.LAYOUT_THEME);
        // modes
        if (modeLinks != null) {
            Iterator modesIt = modeLinks.iterator();
            PortletTitleBar.PortletModeLink mode;
            if (!modesIt.hasNext()) {
                titleBuffer.append("&nbsp;");
            }
            while (modesIt.hasNext()) {
                mode = (PortletTitleBar.PortletModeLink) modesIt.next();
                titleBuffer.append("<a href=\"").append(mode.getHref()).append("\"><img border=\"0\" src=\"").append(req.getContextPath()).append("/themes/").append(renderKit).append("/").append(theme).append("/").append(mode.getImageSrc()).append("\" title=\"").append(mode.getAltTag()).append("\" alt=\"").append(mode.getAltTag()).append("\"");
                if (!mode.getCursor().equals("")) {
                    titleBuffer.append(" style=\"cursor: ").append(mode.getCursor()).append(";\"");
                }
                titleBuffer.append("\" /></a>"); /// Removed File.separator(s)
            }
        }

        // states
        if (windowLinks != null) {
            Iterator windowsIt = windowLinks.iterator();
            PortletTitleBar.PortletStateLink state;
            while (windowsIt.hasNext()) {
                state = (PortletTitleBar.PortletStateLink) windowsIt.next();
                titleBuffer.append("<a href=\"").append(state.getHref()).append("\"><img  border=\"0\" src=\"").append(req.getContextPath()).append("/themes/").append(renderKit).append("/").append(theme).append("/").append(state.getImageSrc()).append("\" title=\"").append(state.getAltTag()).append("\" alt=\"").append(state.getAltTag()).append("\" /></a>");
            }
        }
        titleBuffer.append("</div>");    // title-icon-right
        titleBuffer.append("</div>");   // window-title
        return titleBuffer;
    }
}

