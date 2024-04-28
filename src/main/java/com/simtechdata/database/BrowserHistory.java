package com.simtechdata.database;

import java.util.LinkedList;

public class BrowserHistory {

    private final LinkedList<Link> linkList = new LinkedList<>();
    private final String date;

    public BrowserHistory(String date, Link link) {
        this.date = date;
        this.linkList.addLast(link);
    }

    public String getDate() {
        return date;
    }

    public void addLink(Link link) {
        linkList.addLast(link);
    }

    public LinkedList<Link> getLinkList() {
        return linkList;
    }

    public int getCount() {
        return linkList.size();
    }
}
