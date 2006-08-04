<%@ page import="org.gridlab.gridsphere.layout.PortletTab"%>
<%@ taglib uri="/portletUI" prefix="ui" %>
<%@ taglib uri="/portletAPI" prefix="portletAPI" %>


<portletAPI:init/>

<jsp:useBean id="actionURI"  class="java.lang.String" scope="request"/>
<jsp:useBean id="controlUI"  class="java.lang.String" scope="request"/>

<% PortletTab tab = (PortletTab)request.getAttribute("portletComp"); %>
<%  String label;
    if (request.getAttribute("isnewtab") != null) {
        label = "Create new subtab";
    } else {
        label = "Edit subtab: <b>" + tab.getTitle("en") + "</b>";
    }
%>

<ui:group label="<%= label %>">
    <ui:table>
        <ui:tablerow>
            <ui:tablecell>
                <ui:actionsubmit beanId="moveLeftButton" action="doMoveTabLeft" value="Move left"/>
            </ui:tablecell>
            <ui:tablecell>
                <ui:actionsubmit beanId="moveRightButton" action="doMoveTabRight" value="Move right"/>
            </ui:tablecell>
        </ui:tablerow>
    </ui:table>
    <ui:table>
        <ui:tablerow>
            <ui:tablecell>
                Edit tab name: <ui:textfield beanId="nameTF" value="<%= tab.getTitle("en") %>"/>
            </ui:tablecell>
        </ui:tablerow>
        <ui:tablerow>
            <ui:tablecell>
                Select required role: <ui:listbox beanId="rolesLB"/>
            </ui:tablecell>
        </ui:tablerow>
        <ui:tablerow>
            <ui:tablecell>
                Edit label used for bookmarking: <ui:textfield beanId="labelTF" value="<%= tab.getLabel() %>"/>
            </ui:tablecell>
        </ui:tablerow>
        <ui:tablerow>
            <ui:tablecell>
                Select column layout:
                <ui:listbox beanId="colsLB"/>
            </ui:tablecell>
        </ui:tablerow>
    </ui:table>
    <ui:table>
        <ui:tablerow>
            <ui:tablecell>
                <% if (request.getAttribute("isnewtab") != null) { %>
                <ui:actionsubmit action="doSaveNewTab" value="Save"/>
                <% } else { %>
                <ui:actionsubmit action="doSaveTab" value="Save"/>
                <% } %>
            </ui:tablecell>
            <ui:tablecell>
                <% if (request.getAttribute("isnewtab") != null) { %>
                <ui:actionsubmit action="doCancel" value="Cancel"/>
                <% } else { %>
                <ui:actionsubmit action="doDeleteTab" value="Delete"/>
                <% } %>
            </ui:tablecell>
        </ui:tablerow>
    </ui:table>

</ui:group>