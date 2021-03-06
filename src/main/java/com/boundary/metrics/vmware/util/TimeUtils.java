// Copyright 2014 Boundary, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.boundary.metrics.vmware.util;

import com.google.common.base.Function;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

public class TimeUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TimeUtils.class);

    /**
     * Most implementations are thread safe but its not guaranteed
     */
    final private static ThreadLocal<DatatypeFactory> datatypeFactoryHolder = new ThreadLocal<DatatypeFactory>()
    {
        @Override
        protected DatatypeFactory initialValue()
        {
            try {
                return DatatypeFactory.newInstance();
            } catch (DatatypeConfigurationException e) {
                throw new IllegalStateException("Failed to create " + DatatypeFactory.class.getSimpleName(), e);
            }
        }
    };


    private TimeUtils() { /* static class */ }

    public static Function<DateTime, XMLGregorianCalendar> toXMLGregorianCalendar() {
        return jodaToXml;
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(@Nullable DateTime time) {
        return jodaToXml.apply(time);
    }

    private static final Function<DateTime, XMLGregorianCalendar> jodaToXml = new Function<DateTime, XMLGregorianCalendar>() {
        @Nullable
        @Override
        public XMLGregorianCalendar apply(@Nullable DateTime input) {
            if (input == null) {
                return null;
            } else {
                GregorianCalendar gc = new GregorianCalendar();
                gc.setTimeInMillis(input.getMillis());
                return datatypeFactoryHolder.get().newXMLGregorianCalendar(gc);
            }
        }
    };

    public static Function<XMLGregorianCalendar, DateTime> toDateTime() {
        return xmlToJoda;
    }

    public static DateTime toDateTime(@Nullable XMLGregorianCalendar time) {
        return xmlToJoda.apply(time);
    }

    private static final Function<XMLGregorianCalendar, DateTime> xmlToJoda = new Function<XMLGregorianCalendar, DateTime>() {
        @Nullable
        @Override
        public DateTime apply(@Nullable XMLGregorianCalendar input) {
            return input == null ? null : new DateTime(input.toGregorianCalendar().getTime());
        }
    };

}
