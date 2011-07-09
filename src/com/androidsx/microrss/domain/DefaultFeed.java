/*
 * Copyright (c) 2009 Omar Pera Mira, Pablo Pera Mira
 * 
 * You can find a copy of the MIT License along with this source file. Otherwise see
 * <http://www.opensource.org/licenses/mit-license.php>
 */
package com.androidsx.microrss.domain;

import java.util.Date;

public class DefaultFeed implements Feed {

    private static final long serialVersionUID = -8482286019210863061L;
    
    private final Date lastModificationDate;
    private final String title;
    private final String url;

    public DefaultFeed(String title, String url, Date lastModificationDate) {
        this.title = title;
        this.url = url;
        this.lastModificationDate = lastModificationDate;

    }

    @Override
    public Date getLastModificationDate() {
        return lastModificationDate;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getURL() {
        return url;
    }

    @Override
    public String toString() {
      return "[" + title + ", " + url + "]";
    }
}
