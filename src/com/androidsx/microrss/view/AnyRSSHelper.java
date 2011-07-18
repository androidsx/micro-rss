package com.androidsx.microrss.view;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

class AnyRSSHelper {

    private static final long SECOND = 1000;
    private static final long MINUTE = 60 * SECOND;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;
    private static final long MONTH = 30 * DAY;
    
  /**
   * Indicates whether the specified action can be used as an intent. This
   * method queries the package manager for installed packages that can
   * respond to an intent with the specified action. If no suitable package is
   * found, this method returns false.
   *
   * @param context The application's environment.
   * @param action The Intent action to check for availability.
   *
   * @return True if an Intent with the specified action can be sent and
   *         responded to, false otherwise.
   */
  public static boolean isIntentAvailable(Context context, String action) {
      final PackageManager packageManager = context.getPackageManager();
      final Intent intent = new Intent(action);
      List<ResolveInfo> list =
              packageManager.queryIntentActivities(intent,
                      PackageManager.MATCH_DEFAULT_ONLY);
      return list.size() > 0;
  }

  /**
   * Converts a {@link Date} to a relative timestamp like '20 minutes ago' or
   * '3 days ago'. If the date is 0 or negative, it returns an empty string.
   * 
   * @param date the date to be converted
   * @return String relative time from the actual date
   */
  public static String toRelativeDateString(Date date) {
    return date.getTime() == 0 ? ""
               : secondsToString(new Date().getTime() - date.getTime());
  }

  private static String secondsToString(long delta) {
      if (delta <= 0) {
        return "";
      }
      if (delta < 1 * MINUTE) {
        return delta == SECOND ? "one second ago"
                        : ((long) delta / SECOND) + " seconds ago";
      }
      if (delta < 2 * MINUTE) {
        return "a minute ago";
      }
      if (delta < 45 * MINUTE) {
        return ((long) delta / MINUTE) + " minutes ago";
      }
      if (delta < 90 * MINUTE) {
        return "an hour ago";
      }
      if (delta < 24 * HOUR) {
        return delta / HOUR + " hours ago";
      }
      if (delta < 48 * HOUR) {
        return "yesterday";
      }
      if (delta < 30 * DAY) {
        return ((long) delta / DAY) + " days ago";
      }
      if (delta < 12 * MONTH) {
        long months = delta / DAY;
        months /= 30;
        return months <= 1 ? "one month ago" : months + " months ago";
      } else {
        long years = delta / DAY;
        years /= 365;
        return years <= 1 ? "one year ago" : years + " years ago";
      }
  }
  
  /**
   * Removes all html tags from a {@link String} and trims the resulting string.
   * 
   * @param content String that contains html tags
   * @return the String without html tags
   */
  public static String removeHtmlTags(String content) {
    return content.replaceAll("\\<.*?\\>", "").trim();
  }
  
  /**
   * Removes all consecutive tabs, whitespaces and line carriages
   * from a {@link String}, event in the middle of the string.
   * 
   * It is usually used after removing HTML tags, where there are
   * duplicated tabs, line carriages, etc.
   * 
   * @param content String that may contain duplicated whitespaces
   * @return the String with no more than 2 consecutive spaces, no tabs and
   * no more than 2 line carriages.
   */
  public static String removeWhitespaceSubstrings(String content) {
    return content.replaceAll(" +", " ").replaceAll("\t+", "")
    .replaceAll("\n{3,}", "\n\n");
  }
  
  /** Map with the key HTML entities and value UNICODE characters */
  private static HashMap<String,Integer> htmlEntities = new HashMap<String,Integer>();
  static {
    htmlEntities.put("n"+"b"+"s"+"p", new Integer(160));
    htmlEntities.put("i"+"e"+"x"+"c"+"l", new Integer(161));
    htmlEntities.put("cent", new Integer(162));
    htmlEntities.put("pound", new Integer(163));
    htmlEntities.put("c"+"u"+"r"+"r"+"e"+"n", new Integer(164));
    htmlEntities.put("y"+"e"+"n", new Integer(165));
    htmlEntities.put("b"+"r"+"v"+"b"+"a"+"r", new Integer(166));
    htmlEntities.put("sect", new Integer(167));
    htmlEntities.put("u"+"m"+"l", new Integer(168));
    htmlEntities.put("copy", new Integer(169));
    htmlEntities.put("o"+"r"+"d"+"f", new Integer(170));
    htmlEntities.put("l"+"a"+"quo", new Integer(171));
    htmlEntities.put("not", new Integer(172));
    htmlEntities.put("shy", new Integer(173));
    htmlEntities.put("r"+"e"+"g", new Integer(174));
    htmlEntities.put("m"+"a"+"c"+"r", new Integer(175));
    htmlEntities.put("d"+"e"+"g", new Integer(176));
    htmlEntities.put("plus"+"m"+"n", new Integer(177));
    htmlEntities.put("sup2", new Integer(178));
    htmlEntities.put("sup3", new Integer(179));
    htmlEntities.put("acute", new Integer(180));
    htmlEntities.put("m"+"i"+"c"+"r"+"o", new Integer(181));
    htmlEntities.put("par"+"a", new Integer(182));
    htmlEntities.put("mid"+"dot", new Integer(183));
    htmlEntities.put("c"+"e"+"d"+"i"+"l", new Integer(184));
    htmlEntities.put("sup1", new Integer(185));
    htmlEntities.put("o"+"r"+"d"+"m", new Integer(186));
    htmlEntities.put("r"+"a"+"quo", new Integer(187));
    htmlEntities.put("frac14", new Integer(188));
    htmlEntities.put("frac12", new Integer(189));
    htmlEntities.put("frac34", new Integer(190));
    htmlEntities.put("i"+"quest", new Integer(191));
    htmlEntities.put("A"+"grave", new Integer(192));
    htmlEntities.put("A"+"a"+"cute", new Integer(193));
    htmlEntities.put("A"+"c"+"i"+"r"+"c", new Integer(194));
    htmlEntities.put("A"+"tilde", new Integer(195));
    htmlEntities.put("A"+"u"+"m"+"l", new Integer(196));
    htmlEntities.put("A"+"ring", new Integer(197));
    htmlEntities.put("A"+"E"+"l"+"i"+"g", new Integer(198));
    htmlEntities.put("C"+"c"+"e"+"d"+"i"+"l", new Integer(199));
    htmlEntities.put("E"+"grave", new Integer(200));
    htmlEntities.put("E"+"a"+"cute", new Integer(201));
    htmlEntities.put("E"+"c"+"i"+"r"+"c", new Integer(202));
    htmlEntities.put("E"+"u"+"m"+"l", new Integer(203));
    htmlEntities.put("I"+"grave", new Integer(204));
    htmlEntities.put("I"+"a"+"cute", new Integer(205));
    htmlEntities.put("I"+"c"+"i"+"r"+"c", new Integer(206));
    htmlEntities.put("I"+"u"+"m"+"l", new Integer(207));
    htmlEntities.put("ETH", new Integer(208));
    htmlEntities.put("N"+"tilde", new Integer(209));
    htmlEntities.put("O"+"grave", new Integer(210));
    htmlEntities.put("O"+"a"+"cute", new Integer(211));
    htmlEntities.put("O"+"c"+"i"+"r"+"c", new Integer(212));
    htmlEntities.put("O"+"tilde", new Integer(213));
    htmlEntities.put("O"+"u"+""+"m"+"l", new Integer(214));
    htmlEntities.put("times", new Integer(215));
    htmlEntities.put("O"+"slash", new Integer(216));
    htmlEntities.put("U"+"grave", new Integer(217));
    htmlEntities.put("U"+"a"+"cute", new Integer(218));
    htmlEntities.put("U"+"c"+"i"+"r"+"c", new Integer(219));
    htmlEntities.put("U"+"u"+"m"+"l", new Integer(220));
    htmlEntities.put("Y"+"a"+"cute", new Integer(221));
    htmlEntities.put("THORN", new Integer(222));
    htmlEntities.put("s"+"z"+"l"+"i"+"g", new Integer(223));
    htmlEntities.put("a"+"grave", new Integer(224));
    htmlEntities.put("a"+"a"+"cute", new Integer(225));
    htmlEntities.put("a"+"c"+"i"+"r"+"c", new Integer(226));
    htmlEntities.put("a"+"tilde", new Integer(227));
    htmlEntities.put("a"+"u"+"m"+"l", new Integer(228));
    htmlEntities.put("a"+"ring", new Integer(229));
    htmlEntities.put("a"+"e"+"l"+"i"+"g", new Integer(230));
    htmlEntities.put("c"+"c"+"e"+"d"+"i"+"l", new Integer(231));
    htmlEntities.put("e"+"grave", new Integer(232));
    htmlEntities.put("e"+"a"+"cute", new Integer(233));
    htmlEntities.put("e"+"c"+"i"+"r"+"c", new Integer(234));
    htmlEntities.put("e"+"u"+"m"+"l", new Integer(235));
    htmlEntities.put("i"+"grave", new Integer(236));
    htmlEntities.put("i"+"a"+"cute", new Integer(237));
    htmlEntities.put("i"+"c"+"i"+"r"+"c", new Integer(238));
    htmlEntities.put("i"+"u"+""+"m"+"l", new Integer(239));
    htmlEntities.put("e"+"t"+"h", new Integer(240));
    htmlEntities.put("n"+"tilde", new Integer(241));
    htmlEntities.put("o"+"grave", new Integer(242));
    htmlEntities.put("o"+"a"+"cute", new Integer(243));
    htmlEntities.put("o"+"c"+"i"+"r"+"c", new Integer(244));
    htmlEntities.put("o"+"tilde", new Integer(245));
    htmlEntities.put("o"+"u"+"m"+"l", new Integer(246));
    htmlEntities.put("divide", new Integer(247));
    htmlEntities.put("o"+"slash", new Integer(248));
    htmlEntities.put("u"+"grave", new Integer(249));
    htmlEntities.put("u"+"a"+"cute", new Integer(250));
    htmlEntities.put("u"+"c"+"i"+"r"+"c", new Integer(251));
    htmlEntities.put("u"+"u"+"m"+"l", new Integer(252));
    htmlEntities.put("y"+"a"+"cute", new Integer(253));
    htmlEntities.put("thorn", new Integer(254));
    htmlEntities.put("y"+"u"+"m"+"l", new Integer(255));
    htmlEntities.put("f"+"no"+"f", new Integer(402));
    htmlEntities.put("Alpha", new Integer(913));
    htmlEntities.put("Beta", new Integer(914));
    htmlEntities.put("Gamma", new Integer(915));
    htmlEntities.put("Delta", new Integer(916));
    htmlEntities.put("Epsilon", new Integer(917));
    htmlEntities.put("Z"+"e"+"t"+"a", new Integer(918));
    htmlEntities.put("E"+"t"+"a", new Integer(919));
    htmlEntities.put("T"+"h"+"e"+"t"+"a", new Integer(920));
    htmlEntities.put("I"+"o"+"t"+"a", new Integer(921));
    htmlEntities.put("K"+"a"+"p"+"pa", new Integer(922));
    htmlEntities.put("Lambda", new Integer(923));
    htmlEntities.put("M"+"u", new Integer(924));
    htmlEntities.put("N"+"u", new Integer(925));
    htmlEntities.put("Xi", new Integer(926));
    htmlEntities.put("O"+"m"+"i"+"c"+"r"+"on", new Integer(927));
    htmlEntities.put("Pi", new Integer(928));
    htmlEntities.put("R"+"h"+"o", new Integer(929));
    htmlEntities.put("S"+"i"+"g"+"m"+"a", new Integer(931));
    htmlEntities.put("Tau", new Integer(932));
    htmlEntities.put("Up"+"s"+"i"+"l"+"on", new Integer(933));
    htmlEntities.put("P"+"h"+"i", new Integer(934));
    htmlEntities.put("C"+"h"+"i", new Integer(935));
    htmlEntities.put("P"+"s"+"i", new Integer(936));
    htmlEntities.put("O"+"m"+"e"+"g"+"a", new Integer(937));
    htmlEntities.put("alpha", new Integer(945));
    htmlEntities.put("beta", new Integer(946));
    htmlEntities.put("gamma", new Integer(947));
    htmlEntities.put("delta", new Integer(948));
    htmlEntities.put("epsilon", new Integer(949));
    htmlEntities.put("z"+"e"+"t"+"a", new Integer(950));
    htmlEntities.put("e"+"t"+"a", new Integer(951));
    htmlEntities.put("the"+"t"+"a", new Integer(952));
    htmlEntities.put("i"+"o"+"t"+"a", new Integer(953));
    htmlEntities.put("k"+"a"+"p"+"pa", new Integer(954));
    htmlEntities.put("lambda", new Integer(955));
    htmlEntities.put("m"+"u", new Integer(956));
    htmlEntities.put("n"+"u", new Integer(957));
    htmlEntities.put("xi", new Integer(958));
    htmlEntities.put("o"+"m"+"i"+""+"c"+"r"+"on", new Integer(959));
    htmlEntities.put("pi", new Integer(960));
    htmlEntities.put("r"+"h"+"o", new Integer(961));
    htmlEntities.put("s"+"i"+"g"+"m"+"a"+"f", new Integer(962));
    htmlEntities.put("s"+"i"+"g"+"m"+"a", new Integer(963));
    htmlEntities.put("tau", new Integer(964));
    htmlEntities.put("up"+"s"+"i"+"l"+"on", new Integer(965));
    htmlEntities.put("p"+"h"+"i", new Integer(966));
    htmlEntities.put("c"+"h"+"i", new Integer(967));
    htmlEntities.put("p"+"s"+"i", new Integer(968));
    htmlEntities.put("o"+"m"+"e"+"g"+"a", new Integer(969));
    htmlEntities.put("the"+"t"+"a"+"s"+"y"+"m", new Integer(977));
    htmlEntities.put("up"+"s"+"i"+"h", new Integer(978));
    htmlEntities.put("pi"+"v", new Integer(982));
    htmlEntities.put("bull", new Integer(8226));
    htmlEntities.put("hell"+"i"+"p", new Integer(8230));
    htmlEntities.put("prime", new Integer(8242));
    htmlEntities.put("Prime", new Integer(8243));
    htmlEntities.put("o"+"line", new Integer(8254));
    htmlEntities.put("f"+"r"+""+"a"+"s"+"l", new Integer(8260));
    htmlEntities.put("we"+"i"+"e"+"r"+"p", new Integer(8472));
    htmlEntities.put("image", new Integer(8465));
    htmlEntities.put("real", new Integer(8476));
    htmlEntities.put("trade", new Integer(8482));
    htmlEntities.put("ale"+"f"+"s"+"y"+"m", new Integer(8501));
    htmlEntities.put("l"+"a"+"r"+"r", new Integer(8592));
    htmlEntities.put("u"+"a"+"r"+"r", new Integer(8593));
    htmlEntities.put("r"+"a"+"r"+"r", new Integer(8594));
    htmlEntities.put("d"+"a"+"r"+"r", new Integer(8595));
    htmlEntities.put("ha"+"r"+"r", new Integer(8596));
    htmlEntities.put("c"+"r"+""+"a"+"r"+"r", new Integer(8629));
    htmlEntities.put("lArr", new Integer(8656));
    htmlEntities.put("uArr", new Integer(8657));
    htmlEntities.put("rArr", new Integer(8658));
    htmlEntities.put("dArr", new Integer(8659));
    htmlEntities.put("hArr", new Integer(8660));
    htmlEntities.put("for"+"all", new Integer(8704));
    htmlEntities.put("part", new Integer(8706));
    htmlEntities.put("exist", new Integer(8707));
    htmlEntities.put("empty", new Integer(8709));
    htmlEntities.put("n"+"a"+"b"+"l"+"a", new Integer(8711));
    htmlEntities.put("is"+"in", new Integer(8712));
    htmlEntities.put("not"+"in", new Integer(8713));
    htmlEntities.put("n"+"i", new Integer(8715));
    htmlEntities.put("p"+"rod", new Integer(8719));
    htmlEntities.put("sum", new Integer(8721));
    htmlEntities.put("minus", new Integer(8722));
    htmlEntities.put("low"+"as"+"t", new Integer(8727));
    htmlEntities.put("r"+"a"+"d"+"i"+"c", new Integer(8730));
    htmlEntities.put("prop", new Integer(8733));
    htmlEntities.put("in"+"fin", new Integer(8734));
    htmlEntities.put("an"+"g", new Integer(8736));
    htmlEntities.put("and", new Integer(8743));
    htmlEntities.put("or", new Integer(8744));
    htmlEntities.put("cap", new Integer(8745));
    htmlEntities.put("cup", new Integer(8746));
    htmlEntities.put("int", new Integer(8747));
    htmlEntities.put("there4", new Integer(8756));
    htmlEntities.put("s"+"i"+"m", new Integer(8764));
    htmlEntities.put("c"+"on"+"g", new Integer(8773));
    htmlEntities.put("a"+"s"+"y"+"m"+"p", new Integer(8776));
    htmlEntities.put("n"+"e", new Integer(8800));
    htmlEntities.put("e"+"q"+"u"+"i"+"v", new Integer(8801));
    htmlEntities.put("l"+"e", new Integer(8804));
    htmlEntities.put("g"+"e", new Integer(8805));
    htmlEntities.put("sub", new Integer(8834));
    htmlEntities.put("sup", new Integer(8835));
    htmlEntities.put("n"+"sub", new Integer(8836));
    htmlEntities.put("sub"+"e", new Integer(8838));
    htmlEntities.put("sup"+"e", new Integer(8839));
    htmlEntities.put("o"+"plus", new Integer(8853));
    htmlEntities.put("o"+"times", new Integer(8855));
    htmlEntities.put("per"+"p", new Integer(8869));
    htmlEntities.put("s"+"dot", new Integer(8901));
    htmlEntities.put("l"+"c"+"e"+"i"+"l", new Integer(8968));
    htmlEntities.put("r"+"c"+"e"+"i"+"l", new Integer(8969));
    htmlEntities.put("l"+"floor", new Integer(8970));
    htmlEntities.put("r"+"floor", new Integer(8971));
    htmlEntities.put("lang", new Integer(9001));
    htmlEntities.put("rang", new Integer(9002));
    htmlEntities.put("l"+"o"+"z", new Integer(9674));
    htmlEntities.put("spades", new Integer(9824));
    htmlEntities.put("clubs", new Integer(9827));
    htmlEntities.put("hearts", new Integer(9829));
    htmlEntities.put("d"+"i"+"am"+"s", new Integer(9830));
    htmlEntities.put("quot", new Integer(34));
    htmlEntities.put("amp", new Integer(38));
    htmlEntities.put("lt", new Integer(60));
    htmlEntities.put("gt", new Integer(62));
    htmlEntities.put("OElig", new Integer(338));
    htmlEntities.put("o"+"e"+"l"+"i"+"g", new Integer(339));
    htmlEntities.put("Scar"+"on", new Integer(352));
    htmlEntities.put("scar"+"on", new Integer(353));
    htmlEntities.put("Y"+"u"+"m"+"l", new Integer(376));
    htmlEntities.put("c"+"i"+"r"+"c", new Integer(710));
    htmlEntities.put("tilde", new Integer(732));
    htmlEntities.put("e"+"n"+"s"+"p", new Integer(8194));
    htmlEntities.put("e"+"m"+"s"+"p", new Integer(8195));
    htmlEntities.put("thin"+"s"+"p", new Integer(8201));
    htmlEntities.put("z"+"w"+"n"+"j", new Integer(8204));
    htmlEntities.put("z"+"w"+"j", new Integer(8205));
    htmlEntities.put("l"+"r"+"m", new Integer(8206));
    htmlEntities.put("r"+"l"+"m", new Integer(8207));
    htmlEntities.put("n"+"dash", new Integer(8211));
    htmlEntities.put("m"+"dash", new Integer(8212));
    htmlEntities.put("l"+"s"+"quo", new Integer(8216));
    htmlEntities.put("r"+"s"+"quo", new Integer(8217));
    htmlEntities.put("s"+"b"+"quo", new Integer(8218));
    htmlEntities.put("l"+"d"+"quo", new Integer(8220));
    htmlEntities.put("r"+"d"+"quo", new Integer(8221));
    htmlEntities.put("b"+"d"+"quo", new Integer(8222));
    htmlEntities.put("dagger", new Integer(8224));
    htmlEntities.put("Dagger", new Integer(8225));
    htmlEntities.put("p"+"e"+"r"+"m"+"i"+"l", new Integer(8240));
    htmlEntities.put("l"+"s"+"a"+"quo", new Integer(8249));
    htmlEntities.put("r"+"s"+"a"+"quo", new Integer(8250));
    htmlEntities.put("euro", new Integer(8364));
  }
  
  /**
   * Turn any HTML escape entities in the string into
   * characters and return the resulting string.
   *
   * @param s String to be un-escaped.
   * @return un-escaped String.
   * @throws NullPointerException if s is null.
   */
  public static String unescapeHTML(String s) {
    StringBuffer result = new StringBuffer(s.length());
    int ampInd = s.indexOf("&");
    int lastEnd = 0;
    while (ampInd >= 0) {
      int nextAmp = s.indexOf("&", ampInd + 1);
      int nextSemi = s.indexOf(";", ampInd + 1);
      if (nextSemi != -1 && (nextAmp == -1 || nextSemi < nextAmp)) {
        int value = -1;
        String escape = s.substring(ampInd + 1, nextSemi);
        try {
          if (escape.startsWith("#")) {
            value = Integer.parseInt(escape.substring(1), 10);
          } else {
            if (htmlEntities.containsKey(escape)) {
              value = htmlEntities.get(escape).intValue();
            }
          }
        } catch (NumberFormatException x) {
          // Could not parse the entity,
          // output it verbatim
          return s;
        }
        result.append(s.substring(lastEnd, ampInd));
        lastEnd = nextSemi + 1;
        if (value >= 0 && value <= 0xffff) {
          result.append((char) value);
        } else {
          result.append("&").append(escape).append(";");
        }
      }
      ampInd = nextAmp;
    }
    result.append(s.substring(lastEnd));
    return result.toString();
  }

  /**
   * Helper method that removes all HTML tags contained in the Strings. It
   * is usually used to show on a Textview a piece of information in HTML. 
   * 
   * @param content
   * @return
   */
  public static String cleanHTML(String content) {
    String unescapeHTML = AnyRSSHelper.unescapeHTML(content);
    String removeHtmlTags = AnyRSSHelper.removeHtmlTags(unescapeHTML);
    String removeExtraHtmlLines = removeHtmlTags.trim();
    
    return AnyRSSHelper.removeWhitespaceSubstrings(removeExtraHtmlLines);
  }

    public static String timeFromLastUpdate(long lastUpdate) {
        return secondsToString((System.currentTimeMillis() - lastUpdate) / 1000);
    }
}
