package edu.umbc.dbpedia.util;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class FlexibleDateParser {
    private List<ThreadLocal<SimpleDateFormat>> threadLocals = new  ArrayList<ThreadLocal<SimpleDateFormat>>();

    public FlexibleDateParser(final TimeZone tz){
    	
    	List<String> formats = new ArrayList<String>();
    	formats.add("yyyy-MM-dd");
    	formats.add("dd/MM/yyyy");
    	formats.add("dd-MMM-yy");
    	
    	
        threadLocals.clear();
        for (final String format : formats) {
            ThreadLocal<SimpleDateFormat> dateFormatTL = new ThreadLocal<SimpleDateFormat>() {
                protected SimpleDateFormat initialValue() {
                    SimpleDateFormat sdf = new SimpleDateFormat(format);
                    sdf.setTimeZone(tz); 
                    sdf.setLenient(false);
                    return sdf;
                }
            };
            threadLocals.add(dateFormatTL);
        }       
    }

    public FlexibleDateParser(){
    	
    	List<String> formats = new ArrayList<String>();
    	formats.add("yyyy-MM-dd");
    	formats.add("MM-dd-yy");
    	formats.add("MM-dd-yyyy");
    	formats.add("MMM-dd-yy"); 
    	formats.add("MMM-dd-yyyy");
    	formats.add("MMM dd yy");
    	formats.add("MMM dd yyyy");
    	formats.add("dd-MMM-yy");
    	formats.add("dd-MMM-yyyy");
    	formats.add("dd MMM yy");
    	formats.add("dd MMM yyyy");
    	formats.add("yyyy");
    	
    	
        threadLocals.clear();
        for (final String format : formats) {
            ThreadLocal<SimpleDateFormat> dateFormatTL = new ThreadLocal<SimpleDateFormat>() {
                protected SimpleDateFormat initialValue() {
                    SimpleDateFormat sdf = new SimpleDateFormat(format);
                    sdf.setLenient(false);
                    return sdf;
                }
            };
            threadLocals.add(dateFormatTL);
        }       
    }

    public Date parseDate(String dateStr) {
        for (ThreadLocal<SimpleDateFormat> tl : threadLocals) {
            SimpleDateFormat sdf = tl.get();
            try {
                return sdf.parse(dateStr);
            } catch (ParseException e) {
                // Ignore and try next date parser
            }
        }
        // All parsers failed
        return null;
    }       

    
    
    public static void main(String[] args) throws IOException {
    	FlexibleDateParser dateParser = new FlexibleDateParser();
    	System.out.println(dateParser.parseDate("2001-12-15"));
    	System.out.println(dateParser.parseDate("June 15,2001"));
    }
    
    
    
}
    