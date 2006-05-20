package org.apache.xmlrpc.util;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;



/** An extension of {@link XmlRpcDateTimeFormat}, which accepts
 * and/or creates instances of {@link Date}.
 */
public abstract class XmlRpcDateTimeDateFormat extends XmlRpcDateTimeFormat {
    private static final long serialVersionUID = -5107387618606150784L;

    public StringBuffer format(Object pCalendar, StringBuffer pBuffer, FieldPosition pPos) {
        final Object cal;
        if (pCalendar != null  &&  pCalendar instanceof Date) {
            Calendar calendar = Calendar.getInstance(getTimeZone());
            calendar.setTime((Date) pCalendar);
            cal = calendar;
        } else {
            cal = pCalendar;
        }
        return super.format(cal, pBuffer, pPos);
    }

    public Object parseObject(String pString, ParsePosition pParsePosition) {
        Calendar cal = (Calendar) super.parseObject(pString, pParsePosition);
        return cal == null ? null : cal.getTime();
    }
}
