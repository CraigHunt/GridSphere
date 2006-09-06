package org.gridsphere.portlets.core.rss;

import org.gridsphere.provider.portlet.ActionPortlet;
import org.gridsphere.provider.event.FormEvent;
import org.gridsphere.provider.portletui.beans.MessageBoxBean;
import org.gridsphere.provider.portletui.beans.MessageStyle;
import org.gridsphere.portlet.*;
import org.gridsphere.portlet.service.PortletServiceException;
import org.gridsphere.services.core.rss.RssService;
import org.gridsphere.portlets.core.BaseGridSpherePortlet;

import javax.servlet.UnavailableException;

import com.sun.syndication.io.FeedException;
import com.sun.syndication.feed.synd.SyndFeed;

import java.net.MalformedURLException;
import java.io.IOException;

public class RSSPortlet extends BaseGridSpherePortlet {

    public final static String VIEW_RSS_JSP = "rss/viewRSS.jsp";
    private RssService rssService = null;

    public void init(PortletConfig config) throws UnavailableException {
        super.init(config);
        try {
            rssService = (RssService) getPortletConfig().getContext().getService(RssService.class);
        } catch (PortletServiceException e) {
            log.error("Unable to initialize services!", e);
        }
    }

    public void initConcrete(PortletSettings settings) throws UnavailableException {
        super.initConcrete(settings);
        DEFAULT_VIEW_PAGE = "doView";
    }


    public void doView(FormEvent event) throws PortletException {
        SyndFeed feed = null;
        PortletApplicationSettings pas = getPortletSettings().getApplicationSettings();
        String feedURL = pas.getAttribute("feedurl");

        //  todo localize the errormessages
        try {
            feed = rssService.getFeed(feedURL);
        } catch (FeedException e) {
            createErrorMessage(event, "Could not create Feed.");
        } catch (MalformedURLException e) {
            createErrorMessage(event, "RSS URL " + feedURL + " is not valid.");
        } catch (IOException e) {
            createErrorMessage(event, "Could not read RSS feed from " + feedURL);
        }
        event.getPortletRequest().setAttribute("rssfeed", feed);
        setNextState(event.getPortletRequest(), VIEW_RSS_JSP);
    }

}