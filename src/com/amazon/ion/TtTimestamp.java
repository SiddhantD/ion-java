/*
 * Copyright (c) 2008 Amazon.com, Inc.  All rights reserved.
 */

package com.amazon.ion;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * An immutable representation of Terrestrial Time (TT), counting milliseconds
 * since the UNIX epoch.
 */
public final class TtTimestamp
    implements Cloneable
{
    public final static Integer UNKNOWN_OFFSET = null;
    public final static Integer UTC_OFFSET = new Integer(0);


    private static final SimpleDateFormat TIMESTAMP_FORMATTER =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    static
    {
        TIMESTAMP_FORMATTER.setLenient(false);
        // TODO share this timezone instance
        TIMESTAMP_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT"));
    }


    /**
     * Milliseconds since the UNIX epoch. Never null.
     */
    private BigDecimal millis;

    /**
     * Minutes offset from UTC; zero means UTC proper,
     * <code>null</code> means that the offset is unknown.
     */
    private Integer localOffset;


    public TtTimestamp(long millis, Integer localOffset) {
        this.millis = new BigDecimal(millis);
        this.localOffset = localOffset;
    }

    /**
     *
     * @param millis must not be {@code null}.
     * @param localOffset may be {@code null} to represent unknown local
     * offset.
     */
    public TtTimestamp(BigDecimal millis, Integer localOffset) {
        if (millis == null) throw new NullPointerException("millis is null");

        // BigDecimal is immutable
        this.millis = millis;
        this.localOffset = localOffset;
    }


    /**
     *
     * @param date must not be {@code null}.
     * @param localOffset may be {@code null} to represent unknown local
     * offset.
     */
    public TtTimestamp(Date date, Integer localOffset) {
        this.millis = new BigDecimal(date.getTime());
        this.localOffset = localOffset;
    }


    @Override
    public TtTimestamp clone()
    {
        return new TtTimestamp(millis, localOffset);
    }


    /**
     * Gets the value of this Ion <code>timestamp</code> as a Java
     * {@link Date}, representing the time in UTC.  As a result, this method
     * will return the same result for all Ion representations of the same
     * instant, regardless of the local offset.
     * <p>
     * Because <code>Date</code> instances are mutable, this method returns a
     * new instance from each call.
     *
     * @return a new <code>Date</code> value, in UTC,
     * or <code>null</code> if <code>this.isNullValue()</code>.
     */
    public Date dateValue()
    {
        return new Date(millis.longValue());
    }


    /**
     * Gets the value of this Ion <code>timestamp</code> as the number of
     * milliseconds since 1970-01-01T00:00:00.000Z, truncating any fractional
     * milliseconds.
     * This method will return the same result for all Ion representations of
     * the same instant, regardless of the local offset.
     *
     * @return the number of milliseconds since 1970-01-01T00:00:00.000Z
     * represented by this timestamp.
     */
    public long getMillis()
    {
        return millis.longValue();
    }

    /**
     * Gets the value of this Ion <code>timestamp</code> as the number of
     * milliseconds since 1970-01-01T00:00:00Z, including fractional
     * milliseconds.
     * This method will return the same result for all Ion representations of
     * the same instant, regardless of the local offset.
     *
     * @return the number of milliseconds since 1970-01-01T00:00:00Z
     * represented by this timestamp.
     */
    public BigDecimal getDecimalMillis()
    {
        return millis;
    }


    /**
     * Gets the local offset (in minutes) of this timestamp, or <code>null</code> if
     * it's unknown (<em>i.e.</em>, <code>-00:00</code>).
     * <p>
     * For example, the result for <code>1969-02-23T07:00+07:00</code> is 420,
     * the result for <code>1969-02-22T22:45:00.00-01:15</code> is -75, and
     * the result for <code>1969-02-23</code> (by Ion's definition, equivalent
     * to <code>1969-02-23T00:00-00:00</code>) is <code>null</code>.
     *
     * @return the positive or negative local-time offset, represented as
     * minutes from UTC.  If the offset is unknown, returns <code>null</code>.
     */
    public Integer getLocalOffset()
    {
        return localOffset;
    }


    /**
     * Prints this timestamp in Ion format.
     *
     * @param out must not be null.
     *
     * @throws IOException propagated when the {@link Appendable} throws it.
     */
    public void print(Appendable out)
        throws IOException
    {
        // Adjust UTC time back to local time
        int deltaMinutes = (localOffset == null ? 0 : localOffset.intValue());
        long deltaMillis = deltaMinutes * 60 * 1000;

        Date dateTimePart = new Date(millis.longValue() + deltaMillis);

        // SimpleDateFormat is not threadsafe!
        String dateTimeRendered;
        synchronized (TIMESTAMP_FORMATTER)
        {
            dateTimeRendered = TIMESTAMP_FORMATTER.format(dateTimePart);
        }
        out.append(dateTimeRendered);

        if (localOffset == null)
        {
            out.append("-00:00");
        }
        else if (deltaMinutes == 0)
        {
            out.append('Z');
        }
        else
        {
            if (deltaMinutes < 0)
            {
                out.append('-');
                deltaMinutes = -deltaMinutes;
            }
            else
            {
                out.append('+');
            }


            int hours   = deltaMinutes / 60;
            int minutes = deltaMinutes - (hours * 60);

            if (hours < 10) {
                out.append('0');
            }
            out.append(Integer.toString(hours));

            out.append(':');

            if (minutes < 10) {
                out.append('0');
            }
            out.append(Integer.toString(minutes));
        }
    }


    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TtTimestamp other = (TtTimestamp) obj;
        if (localOffset == null)
        {
            if (other.localOffset != null)
                return false;
        }
        else if (!localOffset.equals(other.localOffset))
            return false;
        if (millis == null)
        {
            if (other.millis != null)
                return false;
        }
        else if (!millis.equals(other.millis))
            return false;
        return true;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((localOffset == null) ? 0 : localOffset.hashCode());
        result = prime * result + ((millis == null) ? 0 : millis.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder(32);
        try
        {
            print(buffer);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Exception printing to StringBuilder",
                                       e);
        }
        return buffer.toString();
    }
}