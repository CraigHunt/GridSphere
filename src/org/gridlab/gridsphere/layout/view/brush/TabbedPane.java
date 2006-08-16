/*
* @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
* @author <a href="mailto:wehrens@aei.mpg.de">Oliver Wehrens</a>
* @version $Id: TabbedPane.java 4496 2006-02-08 20:27:04Z wehrens $
*/

package org.gridlab.gridsphere.layout.view.brush;

import org.gridlab.gridsphere.layout.PortletComponent;
import org.gridlab.gridsphere.layout.PortletTab;
import org.gridlab.gridsphere.layout.PortletTabbedPane;
import org.gridlab.gridsphere.layout.view.BaseRender;
import org.gridlab.gridsphere.layout.view.TabbedPaneView;
import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
import org.gridlab.gridsphere.portlet.PortletResponse;
import org.gridlab.gridsphere.portlet.PortletRequest;
import org.gridlab.gridsphere.portlet.PortletURI;
import org.gridlab.gridsphere.portlet.impl.SportletProperties;

import java.util.List;
import java.util.StringTokenizer;

public class TabbedPane extends BaseRender implements TabbedPaneView {

    // TAB
    protected static final StringBuffer TAB_START_MENU =
            new StringBuffer("\n<!-- LAYOUT NAVIGATION -->\n <div id=\"gridsphere-layout-navigation\"> \n<ul id=\"gridsphere-nav\"><li id=\"gridsphere-nav-border-left\">&nbsp;</li>\n");
    protected static final StringBuffer TAB_END_MENU =
            new StringBuffer("\n<li id=\"gridsphere-nav-border-right\">&nbsp;</li></ul> <!-- END NAVIGATION (SUB) MENU -->\n");

    // SUB TAB
    protected static final StringBuffer TAB_START_SUBMENU =
            new StringBuffer("\n<ul id=\"gridsphere-sub-nav\"><li id=\"gridsphere-sub-nav-border-left\">&nbsp;</li>\n");
    protected static final StringBuffer TAB_END_SUBMENU =
            new StringBuffer("\n<li id=\"gridsphere-sub-nav-border-right\">&nbsp;</li></ul> <!-- END NAVIGATION (SUB) MENU -->\n");
    
    // SIDEMENU
    protected static final StringBuffer TAB_START_SIDEMENU =
            new StringBuffer("\n<!-- BEGIN NAVIGATION SIDE MENU -->\n")
                .append("<div id=\"gridsphere-layout-body\">\n")
                .append("<div class=\"gridsphere-layout-row\">\n ")
                .append("<div class=\"gridsphere-layout-column\" style=\"width: 20%\">\n")
                .append("<div id=\"gridsphere-layout-sidemenu\">\n")
                .append("<div id=\"button\">\n <ul>\n");

    protected static final StringBuffer TAB_END_SIDEMENU =
            new StringBuffer("</ul>\n</div><!-- END BUTTON -->\n</div> <!-- END SIDEMENU -->\n </div> <!-- END LAYOUT COLUMN -->\n")
                .append("<div class=\"gridsphere-layout-column\" style=\"width: 80%\"> <!-- START MAIN PORTLET AREA -->\n");

    // TOPMENU
    protected static final StringBuffer TAB_START_TOPMENU =
            new StringBuffer("\n<!-- START NAVIGATION TOP MENU-->\n <div id=\"gridsphere-layout-navigation\"> \n<ul id=\"gridsphere-nav\"><li id=\"gridsphere-nav-border-left\">&nbsp;</li>\n");
    protected static final StringBuffer TAB_END_TOPMENU =
            new StringBuffer("\n<li id=\"gridsphere-nav-border-right\">&nbsp;</li></ul></div>\n<!-- END NAVIGATION TOP MENU -->\n");


    private static String TAB_STYLE_MENU = "menu";
    private static String TAB_STYLE_SUBMENU = "sub-menu";
    private static String TAB_STYLE_SIDEMENU = "side-menu";
    private static String TAB_STYLE_TOPMENU = "top-menu";

    /**
     * Constructs an instance of PortletTabbedPane
     */
    public TabbedPane() {
    }


    /**
     * Creates the portlet tab link URIs that are used to send events to
     * the portlet tabs.
     *
     * @param event the gridsphere event
     */
    protected static String[] createTabLinks(GridSphereEvent event, PortletTabbedPane pane) {
        // Make tab links
        String[] tabLinks = new String[pane.getPortletTabs().size()];
        List tabs = pane.getPortletTabs();
        for (int i = 0; i < tabs.size(); i++) {
            PortletTab tab = (PortletTab) tabs.get(i);
            tabLinks[i] = tab.createTabTitleLink(event);
        }
        return tabLinks;
    }

    /**
     * Replace blank spaces in title with '&nbsp;'
     *
     * @param title the tab title
     * @return a title without blank spaces
     */
    private static String replaceBlanks(String title) {
        String result = "&nbsp;";
        StringTokenizer st = new StringTokenizer(title);
        while (st.hasMoreTokens()) {
            result += st.nextToken() + "&nbsp;";
        }
        return result;
    }

    public StringBuffer doStart(GridSphereEvent event, PortletComponent comp) {
        PortletTabbedPane pane = (PortletTabbedPane) comp;
        StringBuffer result = new StringBuffer();
        if (pane.getStyle().equalsIgnoreCase(TAB_STYLE_SUBMENU)) {
            result.append(TAB_START_SUBMENU);
        }
        if (pane.getStyle().equalsIgnoreCase(TAB_STYLE_MENU)) {
            result.append(TAB_START_MENU);
        }
        if (pane.getStyle().equalsIgnoreCase(TAB_STYLE_SIDEMENU)) {
            result.append(TAB_START_SIDEMENU);
        }
        if (pane.getStyle().equalsIgnoreCase(TAB_STYLE_TOPMENU)) {
            result.append(TAB_START_TOPMENU);
        }



        return result;
    }

    public StringBuffer doRenderTab(GridSphereEvent event, PortletTabbedPane tabPane, PortletTab tab) {
        // this really creates the individual tabs
        StringBuffer pane = new StringBuffer();
        String link = tab.createTabTitleLink(event);
        String lang = event.getPortletRequest().getLocale().getLanguage();
        String title = tab.getTitle(lang);

        if (tabPane.getStyle().equalsIgnoreCase(TAB_STYLE_SUBMENU)) {
            pane.append("\n<li");
            if (tab.isSelected()) {
                pane.append(" class=\"sub-nav-sel\"");
            }
            pane.append(">");
            pane.append("<a href=\"").append(link).append("\">").append(replaceBlanks(title)).append("</a></li>\n");
        }
        if (tabPane.getStyle().equalsIgnoreCase(TAB_STYLE_MENU) || tabPane.getStyle().equalsIgnoreCase(TAB_STYLE_TOPMENU)) {
            String selected = "nav-nonsel";
            if (tab.isSelected()) {
                selected = "nav-sel";
            }
            pane.append("<li class=\"").append(selected).append("\">");
            pane.append("<a href=\"").append(link).append("\"><span>").
                    append(replaceBlanks(title)).append("</span></a></li>");
        }
        if (tabPane.getStyle().equalsIgnoreCase(TAB_STYLE_SIDEMENU)) {
            String selected = "";
            if (tab.isSelected()) {
                selected = " class =\"selected\" ";
            }
            pane.append("<li ").append(selected).append("><a href=\"").append(link).append("\">").
                    append(replaceBlanks(title)).append("</a></li>");
        }
        return pane;
    }

    public StringBuffer doRenderEditTab(GridSphereEvent event, PortletTabbedPane tabPane, boolean isSelected) {
        PortletResponse res = event.getPortletResponse();
        PortletRequest req = event.getPortletRequest();
        PortletURI portletURI = res.createURI();
        String extraQuery = (String)req.getAttribute(SportletProperties.EXTRA_QUERY_INFO);
        String link = portletURI.toString() + extraQuery;
        StringBuffer pane = new StringBuffer();

        String compVar = (String)req.getAttribute(SportletProperties.COMPONENT_ID_VAR);
        if (compVar == null) compVar = SportletProperties.COMPONENT_ID;
        String cid = (String)req.getAttribute(compVar);

        if (tabPane.getStyle().equals(TAB_STYLE_SUBMENU)) {
            pane.append("\n<li");
            if (isSelected) {
                pane.append(" class=\"sub-nav-sel\"");
            }
            pane.append(">");
            pane.append("<a href=\"").append(link).append("&newsubtab=true").append("\">").append(replaceBlanks("New subtab")).append("</a></li>\n");
        }
        if (tabPane.getStyle().equals(TAB_STYLE_MENU)) {
            String selected = "nav-nonsel";
            if (isSelected) {
                selected = "nav-sel";
            }
            pane.append("<li class=\"").append(selected).append("\">");
            pane.append("<a href=\"").append(link).append("&newtab=true").append("\"><span>").append(replaceBlanks("New tab")).append("</span></a></li>");
        }

        return pane;
    }

    public StringBuffer doEndBorder(GridSphereEvent event, PortletComponent comp) {
        StringBuffer result = new StringBuffer();
        PortletTabbedPane pane = (PortletTabbedPane) comp;
        if (pane.getStyle().equalsIgnoreCase(TAB_STYLE_SUBMENU)) {
            result.append(TAB_END_SUBMENU);
            result.append("\n</div> <!--  END LAYOUT NAVIGATION -->\n<div id=\"gridsphere-layout-body\"> <!-- start the main portlets -->\n");
        }
        if (pane.getStyle().equalsIgnoreCase(TAB_STYLE_MENU)) {
            result.append(TAB_END_MENU);
        }
        if (pane.getStyle().equalsIgnoreCase(TAB_STYLE_TOPMENU)) {
            result.append(TAB_END_TOPMENU);
        }
        if (pane.getStyle().equalsIgnoreCase(TAB_STYLE_SIDEMENU)) {
            result.append(TAB_END_SIDEMENU);
        }
        return result;
    }

    public StringBuffer doEnd(GridSphereEvent event, PortletComponent comp) {
        StringBuffer buffer = new StringBuffer();
        PortletTabbedPane pane = (PortletTabbedPane) comp;
        if (pane.getStyle().equalsIgnoreCase(TAB_STYLE_MENU)) {
            buffer.append("\n</div> <!-- END gridsphere-layout-body -->\n");
        }
        if (pane.getStyle().equalsIgnoreCase(TAB_STYLE_SIDEMENU)) {
            buffer.append("\n </div> <!-- END LAYOUT COLUMN -->\n </div> <!-- END LAYOUT ROW --> \n</div> <!-- END LAYOUT BODY --> \n<!-- END MAIN PORTLET AREA -->");
        }
        return buffer;
    }
}