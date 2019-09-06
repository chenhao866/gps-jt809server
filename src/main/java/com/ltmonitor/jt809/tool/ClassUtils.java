 package com.ltmonitor.jt809.tool;
 
 import org.apache.log4j.Logger;
 
 public class ClassUtils
 {
   public static Logger logger = Logger.getLogger(ClassUtils.class);
 
   public static Object getBean(String className)
   {
     Class clazz = null;
     try
     {
       clazz = Class.forName(className);
     }
     catch (Exception ex)
     {
    	 logger.error(ex.getMessage(),ex);
     }
     if (clazz != null)
     {
       try
       {
         return clazz.newInstance();
       }
       catch (Exception ex) {
      	 logger.error(ex.getMessage(),ex);
       }
     }
     return null;
   }
 }

