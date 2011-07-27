/*
 * CommandLineParser.java
 * 
 * Copyright (c) 2011, Luca Conte. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.bazu.jotex;

import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;

public class CommandLineLauncher {
  
  private Options options;
  private Parser parser;
 
  
 public static void main(String[] args) {
   OdtEPUBlisher op=new OdtEPUBlisher();
   CommandLineLauncher cl=new CommandLineLauncher();
   try {
     CommandLine line = cl.getParser().parse( cl.getOptions(), args );
     
     if(line.hasOption("h")){
       HelpFormatter formatter = new HelpFormatter();
       formatter.printHelp( "java -jar jotex<VERSION>.jar [<OPTIONS>] <FILE_NAME>.odt", cl.getOptions() );
       System.exit(0);
     }
     
     if(line.hasOption("s")){
       op.setMaxFilesSize(Integer.parseInt(line.getOptionValue("s")));
     }
     
     if(line.hasOption("d")){
       op.setDebugMode(true);
     }
   } catch (ParseException e1) {
     e1.printStackTrace();
     System.out.println("Bad input oprions");
   }
   
   if(args.length==0||!args[args.length-1].toUpperCase().endsWith(".ODT")){
     HelpFormatter formatter = new HelpFormatter();
     formatter.printHelp( "java -jar jotex<VERSION>.jar [<OPTIONS>] <FILE_NAME>.odt", CommandLineLauncher.getOptionsInstance() );
     System.exit(1);
   }
   
   
   System.out.println("Exporting process of \""+args[args.length-1]+"\" STARTED at "+new Date());
   
 
   op.setOdtFilename(args[args.length-1]);
   op.setEpubFilename(args[args.length-1].replaceFirst("\\.odt", ".epub"));
   try {
     op.startRippingSession();
   } catch (Exception e) {
     e.printStackTrace();
      System.out.println("Exporting process FAILED at "+new Date());
   }
   System.out.println("Exporting process of \""+op.getEpubFilename()+"\" FINISHED at "+new Date());
 }
  public static Options getOptionsInstance(){
    PosixParser parser = new PosixParser();

 // create the Options
    Options options = new Options();
   options.addOption( "h", "help", false, "Print Jotex comman line help" );
   options.addOption( "d", false, "Enable debug mode" );
   // options.addOption( "A", "almost-all", false, "do not list implied . and .." );
   // options.addOption( "b", "escape", false, "print octal escapes for nongraphic "
 //                                         + "characters" );
    options.addOption( OptionBuilder.withLongOpt( "max-file-size" )
                                 .withDescription( "Max single xhtml's file size in KB" )
                                 .hasArg()
                                 .withArgName("SIZE")
                                 .withType(Integer.class)
                                 .create("s") );
//    options.addOption( "B", "ignore-backups", false, "do not list implied entried "
//                                                  + "ending with ~");
//    options.addOption( "c", false, "with -lt: sort by, and show, ctime (time of last " 
//                                + "modification of file status information) with "
//                                + "-l:show ctime and sort by name otherwise: sort "
//                                + "by ctime" );
//    options.addOption( "C", false, "list entries by columns" );
    return  options;
  }
  

  
  
  public Options getOptions() {
    if (options == null) {
      options =  getOptionsInstance();
      
    }

    return options;
  }

  public Parser getParser() {
    if (parser == null) {
      parser =   new PosixParser();
      
    }

    return parser;
  }

}
