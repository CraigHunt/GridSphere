package org.gridsphere.portlets.core.admin.layout;

import org.gridsphere.provider.portlet.ActionPortlet;
import org.gridsphere.provider.portlet.jsr.PortletServlet;
import org.gridsphere.provider.event.FormEvent;
import org.gridsphere.provider.portletui.beans.*;
import org.gridsphere.portlet.*;
import org.gridsphere.portlet.service.PortletServiceException;
import org.gridsphere.portlet.impl.SportletProperties;
import org.gridsphere.layout.*;
import org.gridsphere.portletcontainer.GridSphereEvent;
import org.gridsphere.portletcontainer.ApplicationPortlet;
import org.gridsphere.portletcontainer.ConcretePortlet;
import org.gridsphere.portletcontainer.impl.GridSphereEventImpl;
import org.gridsphere.services.core.security.role.RoleManagerService;
import org.gridsphere.services.core.security.role.PortletRole;
import org.gridsphere.services.core.content.ContentManagerService;
import org.gridsphere.services.core.content.ContentFile;
import org.gridsphere.services.core.registry.PortletRegistryService;

import javax.servlet.UnavailableException;
import java.io.IOException;
import java.io.File;
import java.util.*;

public class LayoutManagerPortlet extends ActionPortlet {


    public static final String VIEW_JSP = "admin/layout/view.jsp";

    private static Map pages = new HashMap();
    private static RoleManagerService roleManagerService;
    private static ContentManagerService contentManagerService;

    private PortletRegistryService portletRegistryService = null;
    private PortletPageFactory pageFactory = null;

    private static final String SELECTED_LAYOUT = LayoutManagerPortlet.class.getName() + ".SELECTED_LAYOUT";

    public void init(PortletConfig config) throws UnavailableException {
        super.init(config);
        roleManagerService = (RoleManagerService) config.getContext().getService(RoleManagerService.class);
        contentManagerService = (ContentManagerService) config.getContext().getService(ContentManagerService.class);
        portletRegistryService = (PortletRegistryService) config.getContext().getService(PortletRegistryService.class);
        pageFactory = PortletPageFactory.getInstance();
        DEFAULT_VIEW_PAGE = "doShowLayout";
    }

    public void initConcrete(PortletSettings settings) throws UnavailableException {
        super.initConcrete(settings);
    }


    public void addportlet(FormEvent event) throws PortletException, IOException {
        PortletRequest req = event.getPortletRequest();
        PortletResponse res = event.getPortletResponse();
        PortletContext context = this.getPortletConfig().getContext();
        GridSphereEvent gsevent = new GridSphereEventImpl(context, req, res);
        PortletPage page = (PortletPage)pages.get(req.getPortletSession().getId());
        if (page == null) page = createLayout(event);
        page.actionPerformed(gsevent);
        page.init(req, new ArrayList());
        pageFactory.savePortletPageMaster(page);
    }

    public void editComponent(FormEvent event) throws PortletException, IOException {
        PortletRequest req = event.getPortletRequest();
        PortletResponse res = event.getPortletResponse();
        PortletContext context = this.getPortletConfig().getContext();
        GridSphereEvent gsevent = new GridSphereEventImpl(context, req, res);
        String sessionId = req.getPortletSession().getId();
        PortletPage page = (PortletPage)pages.get(sessionId);
        PortletTabbedPane pane = page.getPortletTabbedPane();


        page.actionPerformed(gsevent);
        pages.put(sessionId, page);

        setNextState(req, DEFAULT_VIEW_PAGE);

    }

    public PortletPage createLayout(FormEvent event) throws PortletException, IOException {
        PortletRequest req = event.getPortletRequest();
        //PortletPage guestPage = pageFactory.createPortletPageCopy(PortletPageFactory.GUEST_PAGE);
        PortletPage page = null;
        PortletSession session = event.getPortletRequest().getPortletSession();
        String layoutId = (String)session.getAttribute(SELECTED_LAYOUT);

        page = pageFactory.createPortletPageCopy(layoutId);

        pageFactory.setPageTheme(page, req);

        page.init(req, new ArrayList());
        return page;
    }


    public void doFinish(FormEvent event) throws PortletException, IOException {
        PortletSession session = event.getPortletRequest().getPortletSession();
        session.setAttribute(SportletProperties.LAYOUT_PAGE, PortletPageFactory.USER_PAGE);
        session.removeAttribute(SELECTED_LAYOUT);
        pages.remove(session.getId());
    }

    public void doSaveFrame(FormEvent event) throws PortletException, IOException {
        PortletRequest req = event.getPortletRequest();

        String sessionId = req.getPortletSession().getId();
        PortletPage page = (PortletPage)pages.get(sessionId);

        HiddenFieldBean compHF = event.getHiddenFieldBean("compHF");
        String activeComp = compHF.getValue();
        String reqRole = event.getListBoxBean("rolesLB").getSelectedName();
        String portletClass = event.getListBoxBean("portletsLB").getSelectedName();
        String label = event.getTextFieldBean("labelTF").getValue();
        PortletComponent comp = page.getActiveComponent(activeComp);

        if (comp instanceof PortletFrame) {
            PortletFrame frame = (PortletFrame)comp;
            frame.setRequiredRole(reqRole);
            frame.setLabel(label);
            frame.setPortletClass(portletClass);

            RadioButtonBean istitleRB = event.getRadioButtonBean("istitleRB");
            if (!istitleRB.getSelectedValue().equalsIgnoreCase("yes")) {
                frame.setTransparent(true);
            }
        }

        pageFactory.savePortletPageMaster(page);

    }

    public void doDeleteFrame(FormEvent event) throws PortletException, IOException {
        PortletRequest req = event.getPortletRequest();
        String sessionId = req.getPortletSession().getId();
        PortletPage page = (PortletPage)pages.get(sessionId);

        HiddenFieldBean compHF = event.getHiddenFieldBean("compHF");
        String activeComp = compHF.getValue();

        PortletComponent comp = page.getActiveComponent(activeComp);

        if (comp instanceof PortletFrame) {
            PortletFrame frame = (PortletFrame)comp;
            PortletComponent parent = frame.getParentComponent();
            parent.remove(frame, req);
        }

        pageFactory.savePortletPageMaster(page);

    }

    public void doSaveTab(FormEvent event) throws PortletException, IOException {
        PortletRequest req = event.getPortletRequest();

        String sessionId = req.getPortletSession().getId();
        PortletPage page = (PortletPage)pages.get(sessionId);


        HiddenFieldBean compHF = event.getHiddenFieldBean("compHF");
        String activeComp = compHF.getValue();
        String reqRole = event.getListBoxBean("rolesLB").getSelectedName();
        String name = event.getTextFieldBean("nameTF").getValue();
        String label = event.getTextFieldBean("labelTF").getValue();
        PortletComponent comp = page.getActiveComponent(activeComp);

        if (comp instanceof PortletTab) {
            PortletTab tab = (PortletTab)comp;
            if (reqRole.equalsIgnoreCase("NONE")) reqRole = "";
            tab.setRequiredRole(reqRole);
            tab.setLabel(label);
            tab.setTitle("en", name);


                ListBoxBean colsLB = event.getListBoxBean("colsLB");
                String colTemplateNum = colsLB.getSelectedName();
            if (colTemplateNum != null) tab = createLayoutStrategy(colTemplateNum, tab);
            PortletTabbedPane parent = (PortletTabbedPane)tab.getParentComponent();
            parent.setSelectedPortletTab(tab);

        }

        pageFactory.savePortletPageMaster(page);
    }

    public void doSaveNewTab(FormEvent event) throws PortletException, IOException {
        PortletRequest req = event.getPortletRequest();

        String sessionId = req.getPortletSession().getId();
        PortletPage page = (PortletPage)pages.get(sessionId);

        HiddenFieldBean compHF = event.getHiddenFieldBean("compHF");
        String activeComp = compHF.getValue();
        String reqRole = event.getListBoxBean("rolesLB").getSelectedName();
        String name = event.getTextFieldBean("nameTF").getValue();
        String label = event.getTextFieldBean("labelTF").getValue();
        PortletComponent comp = page.getActiveComponent(activeComp);
        if (comp instanceof PortletTabbedPane) {
            PortletTabbedPane pane = (PortletTabbedPane)comp;
            //System.err.println("tab name " + thistab.getTitle("en"));
            //PortletTabbedPane pane = (PortletTabbedPane)thistab.getParentComponent();

            PortletTab tab = new PortletTab();
            tab.setTitle("en", name);
            if (reqRole.equalsIgnoreCase("NONE")) reqRole = "";
            tab.setRequiredRole(reqRole);
            tab.setLabel(label);
            if (pane.getStyle().equals("menu")) {
                RadioButtonBean addSubTab = event.getRadioButtonBean("subcompRB");
                if (addSubTab.getSelectedValue().equals("double")) {
                    PortletTabbedPane newpane = new PortletTabbedPane();
                    newpane.setStyle("sub-menu");
                    tab.setPortletComponent(newpane);
                } else {
                    PortletRowLayout row = new PortletRowLayout();
                    PortletColumnLayout col = new PortletColumnLayout();
                    col.setWidth("100%");
                    row.addPortletComponent(col);
                    PortletTableLayout table = new PortletTableLayout();
                    table.addPortletComponent(row);
                    tab.setPortletComponent(table);
                }
            } else if (pane.getStyle().equals("sub-menu")) {
                ListBoxBean colsLB = event.getListBoxBean("colsLB");
                String colTemplateNum = colsLB.getSelectedName();
                tab = createLayoutStrategy(colTemplateNum, tab);
            }
            pane.addTab(tab);
            pane.setSelectedPortletTab(tab);

            pageFactory.savePortletPageMaster(page);


            page.init(req, new ArrayList());

            pages.put(sessionId, page);
        }


    }

    public void doDeleteTab(FormEvent event) throws PortletException, IOException {
        PortletRequest req = event.getPortletRequest();
        String sessionId = req.getPortletSession().getId();
        PortletPage page = (PortletPage)pages.get(sessionId);

        HiddenFieldBean compHF = event.getHiddenFieldBean("compHF");
        String activeComp = compHF.getValue();

        PortletComponent comp = page.getActiveComponent(activeComp);

        if (comp instanceof PortletTab) {
            PortletTab tab = (PortletTab)comp;
            PortletTabbedPane pane = (PortletTabbedPane)tab.getParentComponent();

            int index = pane.getIndexOfTab(tab);

            if (index < (pane.getTabCount() - 1)) {
                pane.setSelectedPortletTabIndex(index+1);
            } else if (index > 0) {
                pane.setSelectedPortletTabIndex(index - 1);
            }
            pane.removeTab(tab);

        }


        pageFactory.savePortletPageMaster(page);
    }


    public void doMoveTabLeft(FormEvent event) throws PortletException, IOException {
        PortletRequest req = event.getPortletRequest();
        String sessionId = req.getPortletSession().getId();
        PortletPage page = (PortletPage)pages.get(sessionId);

        HiddenFieldBean compHF = event.getHiddenFieldBean("compHF");
        String activeComp = compHF.getValue();

        PortletComponent comp = page.getActiveComponent(activeComp);

        if (comp instanceof PortletTab) {
            PortletTab tab = (PortletTab)comp;
            PortletTabbedPane pane = (PortletTabbedPane)tab.getParentComponent();
            int index = pane.getIndexOfTab(tab);
            pane.removeTab(tab);
            pane.insertTab(tab, index-1);
            pane.setSelectedPortletTabIndex(index-1);
        }


        pageFactory.savePortletPageMaster(page);

    }

    public void doMoveTabRight(FormEvent event) throws PortletException, IOException {
        PortletRequest req = event.getPortletRequest();
        String sessionId = req.getPortletSession().getId();
        PortletPage page = (PortletPage)pages.get(sessionId);

        HiddenFieldBean compHF = event.getHiddenFieldBean("compHF");
        String activeComp = compHF.getValue();

        PortletComponent comp = page.getActiveComponent(activeComp);

        if (comp instanceof PortletTab) {
            PortletTab tab = (PortletTab)comp;
            PortletTabbedPane pane = (PortletTabbedPane)tab.getParentComponent();
            int index = pane.getIndexOfTab(tab);
            pane.removeTab(tab);
            pane.insertTab(tab, index+1);
            pane.setSelectedPortletTabIndex(index+1);
        }


        pageFactory.savePortletPageMaster(page);

    }

    public void doCancel(FormEvent event) throws PortletException, IOException {
        // do nothing
    }


    public void doShowLayout(FormEvent event) throws PortletException, IOException {

        PortletRequest req = event.getPortletRequest();
        PortletSession session = event.getPortletRequest().getPortletSession();

        Set layoutIds = pageFactory.getEditableLayoutIds();

        // set guest page as the selected page
        if (session.getAttribute(SELECTED_LAYOUT) == null) {
            session.setAttribute(SELECTED_LAYOUT, PortletPageFactory.GUEST_PAGE);
        }

        String selectedLayout = (String)session.getAttribute(SELECTED_LAYOUT);
        ListBoxBean layoutsLB = event.getListBoxBean("layoutsLB");
        layoutsLB.clear();
        Iterator it = layoutIds.iterator();
        while (it.hasNext()) {
            String layoutId = (String)it.next();
            ListBoxItemBean item = new ListBoxItemBean();
            item.setName(layoutId);
            item.setValue(layoutId);
            if (layoutId.equalsIgnoreCase(selectedLayout)) item.setSelected(true);
            layoutsLB.addBean(item);
        }



        String theme = (String)req.getPortletSession().getAttribute(SportletProperties.LAYOUT_THEME);

        String renderkit = (String)req.getPortletSession().getAttribute(SportletProperties.LAYOUT_RENDERKIT);

        ListBoxBean themesLB = event.getListBoxBean("themesLB");

        themesLB.clear();

        String themesPath = getPortletConfig().getContext().getRealPath("/themes");
        /// retrieve the current renderkit
        themesPath += "/" + renderkit;

        String[] themes = null;
        File f = new File(themesPath);
        if (f.isDirectory()) {
            themes = f.list();
        }

        for (int i = 0; i < themes.length; i++) {
            ListBoxItemBean lb = new ListBoxItemBean();
            lb.setValue(themes[i].trim());
            if (themes[i].trim().equalsIgnoreCase(theme)) lb.setSelected(true);
            themesLB.addBean(lb);
        }


        req.setAttribute(SportletProperties.COMPONENT_ID_VAR, "compid");

        String sessionId = session.getId();
        PortletResponse res = event.getPortletResponse();
        PortletContext context = this.getPortletConfig().getContext();
        GridSphereEvent gsevent = new GridSphereEventImpl(context, req, res);


        // theme has to be set before it is inited
        req.setAttribute(SportletProperties.LAYOUT_EDIT_MODE, "true");

        String actionURI = "?" + SportletProperties.LAYOUT_PAGE_PARAM + "=" + PortletPageFactory.USER_PAGE;
        req.setAttribute("actionURI", actionURI);

        String cid = (String)req.getAttribute(SportletProperties.COMPONENT_ID);
        String extraURI = "&" + SportletProperties.COMPONENT_ID + "=" + cid +
                "&" + SportletProperties.DEFAULT_PORTLET_ACTION + "=doShowLayout";

        req.setAttribute(SportletProperties.EXTRA_QUERY_INFO, extraURI);

        PortletPage page = (PortletPage)pages.get(sessionId);
        if (page == null) page = createLayout(event);

        if (req.getParameter("usertable") != null) {
            addportlet(event);
        }



        String controlUI = "";
        String compid = req.getParameter("compid");
        if (compid == null) compid = cid;

        if (compid == null) log.debug("component is null beeatch!!");
        if (compid != null) {



            PortletComponent comp = page.getActiveComponent(compid);

            if (comp instanceof PortletFrame) {
                log.debug("it's a frame!");
                PortletFrame frame = (PortletFrame)comp;
                // don't perform action on portlet frame
                controlUI = "frame";

                if (!frame.getTransparent()) {
                    req.setAttribute("isTitle", "true");
                }

                ListBoxBean portletsLB = event.getListBoxBean("portletsLB");
                Collection appColl = portletRegistryService.getAllApplicationPortlets();
                Locale loc = req.getLocale();
                Iterator appIt = appColl.iterator();
                while (appIt.hasNext()) {
                    ApplicationPortlet app = (ApplicationPortlet) appIt.next();
                    List concPortlets = app.getConcretePortlets();
                    Iterator cit = concPortlets.iterator();
                    while (cit.hasNext()) {

                        ConcretePortlet conc = (ConcretePortlet) cit.next();
                        String concID = conc.getConcretePortletID();

                        // we don't want to list PortletServlet loader!
                        if (concID.startsWith(PortletServlet.class.getName())) continue;


                        String dispName = conc.getDisplayName(loc);
                        String descName = conc.getDescription(loc);
                        ListBoxItemBean item = new ListBoxItemBean();
                        item.setValue(dispName + " - " + descName);
                        item.setName(concID);
                        if (concID.equalsIgnoreCase(frame.getPortletClass())) item.setSelected(true);
                        portletsLB.addBean(item);
                    }
                }
            } else if (comp instanceof PortletContent) {
                controlUI = "content";
                ListBoxBean contentLB = event.getListBoxBean("contentLB");
                List contentFiles = contentManagerService.getAllContent();
                for (int i = 0; i < contentFiles.size(); i++) {
                    ContentFile contentFile = (ContentFile)contentFiles.get(i);
                    ListBoxItemBean item = new ListBoxItemBean();
                    item.setName(contentFile.getFile().getName());
                    item.setValue(contentFile.getFile().getName());
                    contentLB.addBean(item);
                }                
            } else if (comp instanceof PortletTab) {
                PortletTab tab = (PortletTab)comp;
                PortletTabbedPane pane = (PortletTabbedPane)tab.getParentComponent();
                if (pane.getStyle().equals("menu")) {
                    log.debug("it's a tab!");
                    controlUI = "tab";
                    createColsListBox(event, tab);
                } else {
                    log.debug("it's a subtab");
                    controlUI = "subtab";
                    createColsListBox(event, tab);
                }
                log.debug("tab name=" + tab.getTitle("en"));

                // if selected tab is first tab disable 'move left' button
                if ((pane.getIndexOfTab(tab) == 0)) {
                    ActionSubmitBean moveLeftButton = event.getActionSubmitBean("moveLeftButton");
                    moveLeftButton.setDisabled(true);
                }
                // if selected tab is last tab disable 'move right' buttom
                if ((pane.getIndexOfTab(tab) == (pane.getTabCount() - 1))) {
                    ActionSubmitBean moveRightButton = event.getActionSubmitBean("moveRightButton");
                    moveRightButton.setDisabled(true);
                }
                log.debug("invoking action on tab/subtab");
                page.actionPerformed(gsevent);
            }
            boolean itsanewtab = false;
            if (req.getParameter("newtab") != null) {
                controlUI = "tab";
                TextFieldBean nameTF = event.getTextFieldBean("nameTF");
                nameTF.setValue("New tab");
                itsanewtab = true;
                PortletTab tab = new PortletTab();
                comp = tab;
            }  else if (req.getParameter("newsubtab") != null) {
                controlUI = "subtab";
                TextFieldBean nameTF = event.getTextFieldBean("nameTF");
                nameTF.setValue("New subtab");
                itsanewtab = true;
                PortletTab tab = new PortletTab();
                createColsListBox(event, tab);
                comp = tab;
            }
            if (itsanewtab) {
                TextFieldBean labelTF = event.getTextFieldBean("labelTF");
                labelTF.setValue("");
                ActionSubmitBean moveLeftButton = event.getActionSubmitBean("moveLeftButton");
                moveLeftButton.setDisabled(true);
                ActionSubmitBean moveRightButton = event.getActionSubmitBean("moveRightButton");
                moveRightButton.setDisabled(true);
                req.setAttribute("isnewtab","true");

            }


            ListBoxBean rolesLB = event.getListBoxBean("rolesLB");
            ListBoxItemBean item = new ListBoxItemBean();
            item.setValue("None required");
            item.setName("NONE");


            rolesLB.addBean(item);
            List roles = roleManagerService.getRoles();
            it = roles.iterator();
            while (it.hasNext()) {
                PortletRole role = (PortletRole)it.next();
                item = new ListBoxItemBean();
                item.setValue(role.getName());
                item.setName(role.getName());
                if ((comp != null) && (comp.getRequiredRole() != null)
                        && (comp.getRequiredRole().equalsIgnoreCase(role.getName()))) item.setSelected(true);
                rolesLB.addBean(item);
            }

            HiddenFieldBean compHF = event.getHiddenFieldBean("compHF");
            compHF.setName("compid");
            compHF.setValue(compid);

            req.setAttribute("portletComp", comp);

            req.setAttribute("controlUI", controlUI);
        }

        PortletTabbedPane pane = page.getPortletTabbedPane();
        pane.doRender(gsevent);


        StringBuffer pageBuffer = pane.getBufferedOutput(req);
        req.setAttribute(SportletProperties.COMPONENT_ID_VAR, SportletProperties.COMPONENT_ID);
        req.setAttribute("pane", pageBuffer.toString());
        pages.put(sessionId, page);

        // put old cid back so beans/tags work!!
        req.setAttribute(SportletProperties.COMPONENT_ID, cid);
        // remove special layout attributes so the rest of "real" layout after this portlet renders properly
        req.removeAttribute(SportletProperties.EXTRA_QUERY_INFO);
        req.removeAttribute(SportletProperties.LAYOUT_EDIT_MODE);

        setNextState(req, VIEW_JSP);

    }

    public void selectLayout(FormEvent event) throws PortletException, IOException {
        ListBoxBean layoutsLB = event.getListBoxBean("layoutsLB");
        PortletSession session = event.getPortletRequest().getPortletSession();
        session.setAttribute(SELECTED_LAYOUT, layoutsLB.getSelectedValue());
        pages.remove(session.getId());
    }

    /**
     * Modifies a portlet tab to provide the desired column layout strategy given a tab
     * The numbering is as follows:
     * "one"  - 1 column
     * "two" - 2 col, 33%, 66%
     * "three" - 2 col, 50%, 50%
     * "four" - 2 col, 66%, 33%
     * "five" - 3 col, 33%, 33%, 33%
     * "six" - 3 col 25%, 50%, 25%
     *
     * @param strategyNum  the string as one of the above
     * @param tab a portlet tab
     * @return the updated tab
     */
    private PortletTab createLayoutStrategy(String strategyNum, PortletTab tab) {
        PortletComponent c = tab.getPortletComponent();
        if (c instanceof PortletTableLayout) {
            PortletTableLayout table = (PortletTableLayout)c;
            List rows = table.getPortletComponents();
            if ((rows != null) && (!rows.isEmpty())) {
                c = (PortletComponent)rows.get(0);
                if (c instanceof PortletRowLayout) {
                    PortletRowLayout row = (PortletRowLayout)c;
                    List cols = row.getPortletComponents();
                    if (cols.size() == 1) {
                        if (strategyNum.equals("one")) {
                            return tab;
                        }
                        if (strategyNum.equals("two")) {
                            // deal with case where column layout needs to be extended
                            PortletColumnLayout oldcol = (PortletColumnLayout)cols.get(0);
                            PortletColumnLayout col = new PortletColumnLayout();
                            oldcol.setWidth("33%");
                            col.setWidth("66%");
                            row.addPortletComponent(col);
                        }
                        if (strategyNum.equals("three")) {
                            PortletColumnLayout oldcol = (PortletColumnLayout)cols.get(0);
                            PortletColumnLayout col = new PortletColumnLayout();
                            oldcol.setWidth("50%");
                            col.setWidth("50%");
                            row.addPortletComponent(col);
                        }
                        if (strategyNum.equals("four")) {
                            PortletColumnLayout oldcol = (PortletColumnLayout)cols.get(0);
                            PortletColumnLayout col = new PortletColumnLayout();
                            oldcol.setWidth("66%");
                            col.setWidth("33%");
                            row.addPortletComponent(col);
                        }
                        if (strategyNum.equals("five")) {
                            PortletColumnLayout oldcol = (PortletColumnLayout)cols.get(0);
                            PortletColumnLayout col = new PortletColumnLayout();
                            PortletColumnLayout newcol = new PortletColumnLayout();
                            oldcol.setWidth("33%");
                            col.setWidth("33%");
                            newcol.setWidth("33%");
                            row.addPortletComponent(col);
                            row.addPortletComponent(newcol);
                        }
                        if (strategyNum.equals("six")) {
                            PortletColumnLayout oldcol = (PortletColumnLayout)cols.get(0);
                            PortletColumnLayout col = new PortletColumnLayout();
                            PortletColumnLayout newcol = new PortletColumnLayout();
                            oldcol.setWidth("25%");
                            col.setWidth("50%");
                            newcol.setWidth("25%");
                            row.addPortletComponent(col);
                            row.addPortletComponent(newcol);
                        }
                    }

                    if (cols.size() == 2) {
                        if (strategyNum.equals("one")) {
                            // deal with case where column layout needs to be reduced
                            PortletColumnLayout oldcol = (PortletColumnLayout)cols.get(1);
                            row.removePortletComponent(oldcol);
                        }
                        if (strategyNum.equals("two")) {
                            PortletColumnLayout oldcol = (PortletColumnLayout)cols.get(0);
                            PortletColumnLayout col = (PortletColumnLayout)cols.get(1);
                            oldcol.setWidth("33%");
                            col.setWidth("66%");

                        }
                        if (strategyNum.equals("three")) {
                            PortletColumnLayout oldcol = (PortletColumnLayout)cols.get(0);
                            PortletColumnLayout col = (PortletColumnLayout)cols.get(1);
                            oldcol.setWidth("50%");
                            col.setWidth("50%");

                        }
                        if (strategyNum.equals("four")) {
                            PortletColumnLayout oldcol = (PortletColumnLayout)cols.get(0);
                            PortletColumnLayout col = (PortletColumnLayout)cols.get(1);
                            oldcol.setWidth("66%");
                            col.setWidth("33%");

                        }
                        if (strategyNum.equals("five")) {
                            PortletColumnLayout oldcol = (PortletColumnLayout)cols.get(0);
                            PortletColumnLayout col = (PortletColumnLayout)cols.get(1);
                            PortletColumnLayout newcol = new PortletColumnLayout();
                            oldcol.setWidth("33%");
                            col.setWidth("33%");
                            newcol.setWidth("33%");
                            row.addPortletComponent(newcol);
                        }
                        if (strategyNum.equals("six")) {
                            PortletColumnLayout oldcol = (PortletColumnLayout)cols.get(0);
                            PortletColumnLayout col = (PortletColumnLayout)cols.get(1);
                            PortletColumnLayout newcol = new PortletColumnLayout();
                            oldcol.setWidth("25%");
                            col.setWidth("50%");
                            newcol.setWidth("25%");

                            row.addPortletComponent(newcol);
                        }
                    }

                    if (cols.size() == 3) {
                        if (strategyNum.equals("one")) {
                            // deal with case where column layout needs to be reduced
                            PortletColumnLayout newcol = (PortletColumnLayout)cols.get(2);
                            PortletColumnLayout oldcol = (PortletColumnLayout)cols.get(1);
                            row.removePortletComponent(oldcol);
                            row.removePortletComponent(newcol);
                        }

                        if (strategyNum.equals("two")) {
                            PortletColumnLayout col1 = (PortletColumnLayout)cols.get(0);
                            PortletColumnLayout col2 = (PortletColumnLayout)cols.get(1);
                            PortletColumnLayout col3 = (PortletColumnLayout)cols.get(2);
                            col1.setWidth("33%");
                            col2.setWidth("66%");
                            row.removePortletComponent(col3);

                        }
                        if (strategyNum.equals("three")) {
                            PortletColumnLayout col1 = (PortletColumnLayout)cols.get(0);
                            PortletColumnLayout col2 = (PortletColumnLayout)cols.get(1);
                            PortletColumnLayout col3 = (PortletColumnLayout)cols.get(2);
                            col1.setWidth("50%");
                            col2.setWidth("50%");
                            row.removePortletComponent(col3);
                        }
                        if (strategyNum.equals("four")) {
                            PortletColumnLayout col1 = (PortletColumnLayout)cols.get(0);
                            PortletColumnLayout col2 = (PortletColumnLayout)cols.get(1);
                            PortletColumnLayout col3 = (PortletColumnLayout)cols.get(2);
                            col1.setWidth("66%");
                            col2.setWidth("33%");
                            row.removePortletComponent(col3);
                        }

                        if (strategyNum.equals("five")) {
                            PortletColumnLayout col1 = (PortletColumnLayout)cols.get(0);
                            PortletColumnLayout col2 = (PortletColumnLayout)cols.get(1);
                            PortletColumnLayout col3 = (PortletColumnLayout)cols.get(2);
                            col1.setWidth("33%");
                            col2.setWidth("33%");
                            col3.setWidth("33%");
                        }
                        if (strategyNum.equals("six")) {
                            PortletColumnLayout col1 = (PortletColumnLayout)cols.get(0);
                            PortletColumnLayout col2 = (PortletColumnLayout)cols.get(1);
                            PortletColumnLayout col3 = (PortletColumnLayout)cols.get(2);
                            col1.setWidth("25%");
                            col2.setWidth("50%");
                            col3.setWidth("25%");
                        }

                    }


                }
            }
        } else {
            PortletTableLayout table = new PortletTableLayout();
            PortletRowLayout row = new PortletRowLayout();

            if (strategyNum.equals("one")) {
                PortletColumnLayout col = new PortletColumnLayout();
                col.setWidth("100%");
                row.addPortletComponent(col);
            }

            if (strategyNum.equals("two")) {
                PortletColumnLayout col = new PortletColumnLayout();
                PortletColumnLayout newcol = new PortletColumnLayout();
                col.setWidth("33%");
                row.addPortletComponent(col);
                col.setWidth("66%");
                row.addPortletComponent(newcol);
            }

            if (strategyNum.equals("three")) {
                PortletColumnLayout col = new PortletColumnLayout();
                PortletColumnLayout newcol = new PortletColumnLayout();
                col.setWidth("50%");
                row.addPortletComponent(col);
                col.setWidth("50%");
                row.addPortletComponent(newcol);
            }

            if (strategyNum.equals("four")) {
                PortletColumnLayout col = new PortletColumnLayout();
                PortletColumnLayout newcol = new PortletColumnLayout();
                col.setWidth("66%");
                row.addPortletComponent(col);
                col.setWidth("33%");
                row.addPortletComponent(newcol);
            }

            if (strategyNum.equals("five")) {
                PortletColumnLayout col1 = new PortletColumnLayout();
                PortletColumnLayout col2 = new PortletColumnLayout();
                PortletColumnLayout col3 = new PortletColumnLayout();
                col1.setWidth("33%");
                row.addPortletComponent(col1);
                col2.setWidth("33%");
                row.addPortletComponent(col2);
                col2.setWidth("33%");
                row.addPortletComponent(col3);
            }
            if (strategyNum.equals("six")) {
                PortletColumnLayout col1 = new PortletColumnLayout();
                PortletColumnLayout col2 = new PortletColumnLayout();
                PortletColumnLayout col3 = new PortletColumnLayout();
                col1.setWidth("25%");
                row.addPortletComponent(col1);
                col2.setWidth("50%");
                row.addPortletComponent(col2);
                col2.setWidth("25%");
                row.addPortletComponent(col3);
            }
            table.addPortletComponent(row);
            tab.setPortletComponent(table);
            return tab;

        }

        return tab;
    }




    private void createColsListBox(FormEvent event, PortletTab tab) {
        // TODO  deal with column layouts
        String colType = "one";
        PortletComponent c = tab.getPortletComponent();
        if (c instanceof PortletTableLayout) {
            PortletTableLayout tableLayout = (PortletTableLayout)c;
            List rows = tableLayout.getPortletComponents();
            PortletComponent row = (PortletComponent)rows.get(0);
            if (row instanceof PortletRowLayout) {
                PortletRowLayout r = (PortletRowLayout)row;
                List cols = r.getPortletComponents();
                if (cols.size() == 2) {
                    PortletColumnLayout col = (PortletColumnLayout)cols.get(0);
                    if (col.getWidth().equals("33%")) {
                        colType = "two";
                    }
                    if (col.getWidth().equals("50%")) {
                        colType = "three";
                    }
                    if (col.getWidth().equals("66%")) {
                        colType = "four";
                    }
                }
                if (cols.size() == 3) {
                    PortletColumnLayout col = (PortletColumnLayout)cols.get(0);
                    if (col.getWidth().equals("33%")) {
                        colType = "five";
                    }
                    if (col.getWidth().equals("25%")) {
                        colType = "six";
                    }
                }

            }
        }

        ListBoxBean colsLB = event.getListBoxBean("colsLB");
        colsLB.clear();
        ListBoxItemBean one = new ListBoxItemBean();
        ListBoxItemBean two = new ListBoxItemBean();
        ListBoxItemBean three = new ListBoxItemBean();
        ListBoxItemBean four = new ListBoxItemBean();
        ListBoxItemBean five = new ListBoxItemBean();
        ListBoxItemBean six = new ListBoxItemBean();
        one.setValue("1 column");
        one.setName("one");
        if (colType.equals("one")) one.setSelected(true);
        two.setValue("2 columns / (33%, 66%)");
        two.setName("two");
        if (colType.equals("two")) two.setSelected(true);
        three.setValue("2 columns / (50%, 50%)");
        three.setName("three");
        if (colType.equals("three")) three.setSelected(true);
        four.setValue("2 columns / (66%, 33%)");
        four.setName("four");
        if (colType.equals("four")) four.setSelected(true);
        five.setValue("3 columns / (33%, 33%, 33%)");
        five.setName("five");
        if (colType.equals("five")) five.setSelected(true);
        six.setValue("3 columns / (25%, 50%, 25%)");
        six.setName("six");
        if (colType.equals("six")) six.setSelected(true);
        colsLB.addBean(one);
        colsLB.addBean(two);
        colsLB.addBean(three);
        colsLB.addBean(four);
        colsLB.addBean(five);
        colsLB.addBean(six);
    }

    private void createErrorMessage(FormEvent event, String msg) {
        MessageBoxBean msgBox = event.getMessageBoxBean("msg");
        msgBox.setMessageType(MessageStyle.MSG_ERROR);
        msgBox.setValue(msg);
    }

    private void createSuccessMessage(FormEvent event, String msg) {
        MessageBoxBean msgBox = event.getMessageBoxBean("msg");
        msgBox.setMessageType(MessageStyle.MSG_SUCCESS);
        msgBox.setValue(msg);
    }

}
