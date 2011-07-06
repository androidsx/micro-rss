package com.androidsx.microrss.webservice;

import java.text.SimpleDateFormat;
import java.util.Date;

class DateParser {
    private static final SimpleDateFormat rfc822DateFormats[] = new SimpleDateFormat[] {
        new SimpleDateFormat("EEE, d MMM yy HH:mm:ss z"),
        new SimpleDateFormat("EEE, d MMM yy HH:mm z"),
        new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z"),
        new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss"),
        new SimpleDateFormat("EEE, d MMM yyyy HH:mm z"), 
        new SimpleDateFormat("d MMM yy HH:mm z"),
        new SimpleDateFormat("d MMM yy HH:mm:ss z"), 
        new SimpleDateFormat("d MMM yyyy HH:mm z"),
        new SimpleDateFormat("d MMM yyyy HH:mm:ss z"),
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")}; 
    
    static Date parseDateInRfc822(String pubDateStr) {
        if (pubDateStr == null) {
            // FIXME: Shouldn't we return null instead?
            return new Date();
        } else {
            Date pubDate = null;
            // Try all different date formats, return whenever any of them matches
            for (int i = 0; i < rfc822DateFormats.length; i++) {
                try {
                    pubDate = rfc822DateFormats[i].parse(pubDateStr);
                } catch (java.text.ParseException e) {
                    // Just try another format
                    pubDate = null;
                }

                if (pubDate != null && pubDate.getDate() != 0) {
                    return pubDate;
                }
            }
         // FIXME: Shouldn't we return null instead?
            return new Date();
        }
    }
}
