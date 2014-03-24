/*
 * $Log: DiffPrint.java,v $
 * Revision 1.1  2007/11/30 15:56:08  sitting-duck
 * moved some of the code around
 *
 * Revision 1.2  2007/08/10 11:27:37  sitting-duck
 * Changes based on issues discoverd by "find bugs"
 *
 * Revision 1.1  2007/07/22 19:28:59  sitting-duck
 * Chanaged the package name from dirBuster to DirBuster
 *
 * Revision 1.1  2006/12/14 11:16:38  cvsjames
 * first add
 *
 * Revision 1.5  2004/01/29 02:35:35  stuart
 * Test for out of bounds exception in UnifiedPrint.print_hunk.
 * Add setOutput() to DiffPrint.Base.
 *
 * Revision 1.4  2003/04/22  01:50:47  stuart
 * add Unified format diff
 *
 * Revision 1.3  2003/04/22  01:00:32  stuart
 * added context diff format
 *
 * Revision 1.2  2000/03/02  16:59:54  stuart
 * add GPL
 *
 *
 *Taken from http://www.bmsi.com/java/
 *
 *
 */

package com.sittinglittleduck.DirBuster.diff;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Vector;

interface UnaryPredicate
{
    boolean execute(Object obj);
}

/** A simple framework for printing change lists produced by <code>Diff</code>.
 * @see bmsi.util.Diff
 * @author Stuart D. Gathman
 * Copyright (C) 2000 Business Management Systems, Inc.
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
public class DiffPrint
{
    /** A Base class for printing edit scripts produced by Diff.
      This class divides the change list into "hunks", and calls
      <code>print_hunk</code> for each hunk.  Various utility methods
      are provided as well.
     */
    public abstract static class Base
    {
        protected PrintWriter outfile;
        
        //portcullis added, will be used get the output as a string
        protected StringBuffer stringOut = new StringBuffer();
        
        public void setOutput(Writer wtr)
        {
            outfile = new PrintWriter(wtr);
        }
        private void setupOutput()
        {
            if (outfile == null)
                outfile = new PrintWriter(new OutputStreamWriter(System.out));
        }
        protected Base(Object[] a,Object[] b)
        {
            file0 = a;
            file1 = b;
            
        }
        /** Set to ignore certain kinds of lines when printing
        an edit script.  For example, ignoring blank lines or comments.
         */
        protected UnaryPredicate ignore = null;
        
        /** Set to the lines of the files being compared.
         */
        protected Object[] file0, file1;
        
        /** Divide SCRIPT into pieces by calling HUNKFUN and
       print each piece with PRINTFUN.
       Both functions take one arg, an edit script.
     
       PRINTFUN takes a subscript which belongs together (with a null
       link at the end) and prints it.  */
        public String print_script(Diff.change script)
        {
            setupOutput();
            Diff.change next = script;
            
            while (next != null)
            {
                Diff.change t, end;
                
                /* Find a set of changes that belong together.  */
                t = next;
                end = hunkfun(next);
                
          /* Disconnect them from the rest of the changes,
             making them a hunk, and remember the rest for next iteration.  */
                next = end.link;
                end.link = null;
                //if (DEBUG)
                //  debug_script(t);
                
                /* Print this hunk.  */
                print_hunk(t);
                
                /* Reconnect the script so it will all be freed properly.  */
                end.link = next;
            }
            outfile.flush();
            
            return stringOut.toString();
        }
        
        /** Called with the tail of the script
       and returns the last link that belongs together with the start
       of the tail. */
        
        protected Diff.change hunkfun(Diff.change hunk)
        {
            return hunk;
        }
        
        protected int first0, last0, first1, last1, deletes, inserts;
        
        /** Look at a hunk of edit script and report the range of lines in each file
      that it applies to.  HUNK is the start of the hunk, which is a chain
      of `struct change'.  The first and last line numbers of file 0 are stored
      in *FIRST0 and *LAST0, and likewise for file 1 in *FIRST1 and *LAST1.
      Note that these are internal line numbers that count from 0.
     
      If no lines from file 0 are deleted, then FIRST0 is LAST0+1.
     
      Also set *DELETES nonzero if any lines of file 0 are deleted
      and set *INSERTS nonzero if any lines of file 1 are inserted.
      If only ignorable lines are inserted or deleted, both are
      set to 0.  */
        
        protected void analyze_hunk(Diff.change hunk)
        {
            int f0, l0 = 0, f1, l1 = 0, show_from = 0, show_to = 0;
            int i;
            Diff.change next;
            boolean nontrivial = (ignore == null);
            
            show_from = show_to = 0;
            
            f0 = hunk.line0;
            f1 = hunk.line1;
            
            for (next = hunk; next != null; next = next.link)
            {
                l0 = next.line0 + next.deleted - 1;
                l1 = next.line1 + next.inserted - 1;
                show_from += next.deleted;
                show_to += next.inserted;
                for (i = next.line0; i <= l0 && ! nontrivial; i++)
                    if (!ignore.execute(file0[i]))
                        nontrivial = true;
                for (i = next.line1; i <= l1 && ! nontrivial; i++)
                    if (!ignore.execute(file1[i]))
                        nontrivial = true;
            }
            
            first0 = f0;
            last0 = l0;
            first1 = f1;
            last1 = l1;
            
      /* If all inserted or deleted lines are ignorable,
         tell the caller to ignore this hunk.  */
            
            if (!nontrivial)
                show_from = show_to = 0;
            
            deletes = show_from;
            inserts = show_to;
        }
        
        /** Print the script header which identifies the files compared. */
        protected void print_header(String filea, String fileb)
        {
            setupOutput();
        }
        
        protected abstract void print_hunk(Diff.change hunk);
        
        protected void print_1_line(String pre,Object linbuf)
        {
            stringOut.append(pre + linbuf.toString() + "\r\n");
        }
        
        /** Print a pair of line numbers with SEPCHAR, translated for file FILE.
       If the two numbers are identical, print just one number.
     
       Args A and B are internal line numbers.
       We print the translated (real) line numbers.  */
        
        protected void print_number_range(char sepchar, int a, int b)
        {
      /* Note: we can have B < A in the case of a range of no lines.
         In this case, we should print the line number before the range,
         which is B.  */
            if (++b > ++a)
                stringOut.append("" + a + sepchar + b);
            else
                stringOut.append(b);
        }
        
        public static char change_letter(int inserts, int deletes)
        {
            if (inserts == 0)
                return 'd';
            else if (deletes == 0)
                return 'a';
            else
                return 'c';
        }
    }
    
    /** Print a change list in the standard diff format.
     */
    public static class NormalPrint extends Base
    {
        
        public NormalPrint(Object[] a,Object[] b)
        {
            super(a,b);
        }
        
        /** Print a hunk of a normal diff.
       This is a contiguous portion of a complete edit script,
       describing changes in consecutive lines.  */
        
        protected void print_hunk(Diff.change hunk)
        {
            
            /* Determine range of line numbers involved in each file.  */
            analyze_hunk(hunk);
            if (deletes == 0 && inserts == 0)
                return;
            
            /* Print out the line number header for this hunk */
            print_number_range(',', first0, last0);
            stringOut.append(change_letter(inserts, deletes));
            print_number_range(',', first1, last1);
            stringOut.append("\r\n");
            
            /* Print the lines that the first file has.  */
            if (deletes != 0)
                for (int i = first0; i <= last0; i++)
                    print_1_line("< ", file0[i]);
            
            if (inserts != 0 && deletes != 0)
                stringOut.append("---\r\n");
            
            /* Print the lines that the second file has.  */
            if (inserts != 0)
                for (int i = first1; i <= last1; i++)
                    print_1_line("> ", file1[i]);
        }
    }
    
    /** Prints an edit script in a format suitable for input to <code>ed</code>.
      The edit script must be generated with the reverse option to
      be useful as actual <code>ed</code> input.
     */
    public static class EdPrint extends Base
    {
        
        public EdPrint(Object[] a,Object[] b)
        {
            super(a,b);
        }
        
        /** Print a hunk of an ed diff */
        protected void print_hunk(Diff.change hunk)
        {
            
            /* Determine range of line numbers involved in each file.  */
            analyze_hunk(hunk);
            if (deletes == 0 && inserts == 0)
                return;
            
            /* Print out the line number header for this hunk */
            print_number_range(',', first0, last0);
            stringOut.append(change_letter(inserts, deletes) + "\r\n");
            
            /* Print new/changed lines from second file, if needed */
            if (inserts != 0)
            {
                boolean inserting = true;
                for (int i = first1; i <= last1; i++)
                {
                    /* Resume the insert, if we stopped.  */
                    if (! inserting)
                        stringOut.append(i - first1 + first0 + "a\r\n");
                    inserting = true;
                    
              /* If the file's line is just a dot, it would confuse `ed'.
                 So output it with a double dot, and set the flag LEADING_DOT
                 so that we will output another ed-command later
                 to change the double dot into a single dot.  */
                    
                    if (".".equals(file1[i]))
                    {
                        stringOut.append("..\r\n");
                        stringOut.append(".\r\n");
                        /* Now change that double dot to the desired single dot.  */
                        stringOut.append(i - first1 + first0 + 1 + "s/^\\.\\././" + "\r\n");
                        inserting = false;
                    }
                    else
                        /* Line is not `.', so output it unmodified.  */
                        print_1_line("", file1[i]);
                }
                
                /* End insert mode, if we are still in it.  */
                if (inserting)
                    stringOut.append(".\r\n");
            }
        }
    }
    
    /** Prints an edit script in context diff format.  This and its
    'unified' variation is used for source code patches.
     */
    public static class ContextPrint extends Base
    {
        
        protected int context = 3;
        
        public ContextPrint(Object[] a,Object[] b)
        {
            super(a,b);
        }
        
        protected void print_context_label(String mark, File inf, String label)
        {
            //setupOutput();
            if (label != null)
                stringOut.append(mark + ' ' + label + "\r\n");
            else if (inf.lastModified() > 0)
                // FIXME: use DateFormat to get precise format needed.
                stringOut.append(
                        mark + ' ' + inf.getPath() + '\t' + new Date(inf.lastModified()) + "\r\n");
            else
                /* Don't pretend that standard input is ancient.  */
                stringOut.append(mark + ' ' + inf.getPath() + "\r\n");
        }
        
        public void print_header(String filea,String fileb)
        {
            print_context_label("***", new File(filea), filea);
            print_context_label("---", new File(fileb), fileb);
        }
        
        /** If function_regexp defined, search for start of function. */
        private String find_function(Object[] lines, int start)
        {
            return null;
        }
        
        protected void print_function(Object[] file,int start)
        {
            String function = find_function(file0, first0);
            if (function != null)
            {
                stringOut.append(" ");
                stringOut.append(
                        (function.length() < 40) ? function : function.substring(0,40)
                        );
            }
        }
        
        protected void print_hunk(Diff.change hunk)
        {
            
            /* Determine range of line numbers involved in each file.  */
            
            analyze_hunk(hunk);
            
            if (deletes == 0 && inserts == 0)
                return;
            
            /* Include a context's width before and after.  */
            
            first0 = Math.max(first0 - context, 0);
            first1 = Math.max(first1 - context, 0);
            last0 = Math.min(last0 + context, file0.length - 1);
            last1 = Math.min(last1 + context, file1.length - 1);
            
            
            stringOut.append("***************");
            
      /* If we looked for and found a function this is part of,
         include its name in the header of the diff section.  */
            print_function(file0, first0);
            
            stringOut.append("\r\n");
            stringOut.append("*** ");
            print_number_range(',', first0, last0);
            stringOut.append(" ****\r\n");
            
            if (deletes != 0)
            {
                Diff.change next = hunk;
                
                for (int i = first0; i <= last0; i++)
                {
          /* Skip past changes that apply (in file 0)
             only to lines before line I.  */
                    
                    while (next != null && next.line0 + next.deleted <= i)
                        next = next.link;
                    
                    /* Compute the marking for line I.  */
                    
                    String prefix = " ";
                    if (next != null && next.line0 <= i)
            /* The change NEXT covers this line.
               If lines were inserted here in file 1, this is "changed".
               Otherwise it is "deleted".  */
                        prefix = (next.inserted > 0) ? "!" : "-";
                    
                    print_1_line(prefix, file0[i]);
                }
            }
            
            stringOut.append("--- ");
            print_number_range(',', first1, last1);
            stringOut.append(" ----\r\n");
            
            if (inserts != 0)
            {
                Diff.change next = hunk;
                
                for (int i = first1; i <= last1; i++)
                {
          /* Skip past changes that apply (in file 1)
             only to lines before line I.  */
                    
                    while (next != null && next.line1 + next.inserted <= i)
                        next = next.link;
                    
                    /* Compute the marking for line I.  */
                    
                    String prefix = " ";
                    if (next != null && next.line1 <= i)
            /* The change NEXT covers this line.
               If lines were deleted here in file 0, this is "changed".
               Otherwise it is "inserted".  */
                        prefix = (next.deleted > 0) ? "!" : "+";
                    
                    print_1_line(prefix, file1[i]);
                }
            }
        }
    }
    
    /** Prints an edit script in context diff format.  This and its
    'unified' variation is used for source code patches.
     */
    public static class UnifiedPrint extends ContextPrint
    {
        
        public UnifiedPrint(Object[] a,Object[] b)
        {
            super(a,b);
        }
        
        public void print_header(String filea,String fileb)
        {
            print_context_label("---", new File(filea), filea);
            print_context_label("+++", new File(fileb), fileb);
        }
        
        private void print_number_range(int a, int b)
        {
            //translate_range (file, a, b, &trans_a, &trans_b);
            
      /* Note: we can have B < A in the case of a range of no lines.
         In this case, we should print the line number before the range,
         which is B.  */
            if (b < a)
                stringOut.append(b + ",0");
            //stringOut.append()
            else
                super.print_number_range(',',a,b);
        }
        
        protected void print_hunk(Diff.change hunk)
        {
            /* Determine range of line numbers involved in each file.  */
            analyze_hunk(hunk);
            
            if (deletes == 0 && inserts == 0)
                return;
            
            /* Include a context's width before and after.  */
            
            first0 = Math.max(first0 - context, 0);
            first1 = Math.max(first1 - context, 0);
            last0 = Math.min(last0 + context, file0.length - 1);
            last1 = Math.min(last1 + context, file1.length - 1);
            
            stringOut.append("@@ -");
            //stringOut.append("@@ -");
            print_number_range(first0, last0);
            stringOut.append(" +");
            //stringOut.append(" +");
            print_number_range(first1, last1);
            stringOut.append(" @@");
            //stringOut.append(" @@");
            
      /* If we looked for and found a function this is part of,
         include its name in the header of the diff section.  */
            print_function(file0,first0);
            
            stringOut.append("\r\n");
            
            Diff.change next = hunk;
            int i = first0;
            int j = first1;
            
            while (i <= last0 || j <= last1)
            {
                
                /* If the line isn't a difference, output the context from file 0. */
                
                if (next == null || i < next.line0)
                {
                    if (i < file0.length)
                    {
                        stringOut.append(' ');
                        stringOut.append(' ');
                        print_1_line("", file0[i++]);
                    }
                    j++;
                }
                else
                {
                    /* For each difference, first output the deleted part. */
                    
                    int k = next.deleted;
                    while (k-- > 0)
                    {
                        stringOut.append('-');
                        stringOut.append('-');
                        print_1_line("", file0[i++]);
                    }
                    
                    /* Then output the inserted part. */
                    
                    k = next.inserted;
                    while (k-- > 0)
                    {
                        stringOut.append('+');
                        stringOut.append('+');
                        print_1_line("", file1[j++]);
                    }
                    
                    /* We're done with this hunk, so on to the next! */
                    
                    next = next.link;
                }
            }
        }
    }
    
    
    /** Read a text file into an array of String.  This provides basic diff
     functionality.  A more advanced diff utility will use specialized
     objects to represent the text lines, with options to, for example,
     convert sequences of whitespace to a single space for comparison
     purposes.
     */
    static String[] slurp(String file) throws IOException
    {
        BufferedReader rdr = new BufferedReader(new FileReader(file));
        try
        {
            
            Vector s = new Vector();
            for (;;)
            {
                String line = rdr.readLine();
                if (line == null) break;
                s.addElement(line);
            }
            String[] a = new String[s.size()];
            s.copyInto(a);
            return a;
        }
        finally
        {
            rdr.close();
        }
    }
    
    public static void main(String[] argv) throws IOException
    {
        String filea = argv[argv.length - 2];
        String fileb = argv[argv.length - 1];
        String[] a = slurp(filea);
        String[] b = slurp(fileb);
        Diff d = new Diff(a,b);
        char style = 'n';
        for (int i = 0; i < argv.length - 2; ++i)
        {
            String f = argv[i];
            if (f.startsWith("-"))
            {
                for (int j = 1; j < f.length(); ++j)
                {
                    switch (f.charAt(j))
                    {
                        case 'e':	// Ed style
                            style = 'e'; break;
                        case 'c':	// Context diff
                            style = 'c'; break;
                        case 'u':
                            style = 'u'; break;
                    }
                }
            }
        }
        boolean reverse = style == 'e';
        Diff.change script = d.diff_2(reverse);
        if (script == null)
            System.err.println("No differences");
        else
        {
            Base p;
            switch (style)
            {
                case 'e':
                    p = new EdPrint(a,b); break;
                case 'c':
                    p = new ContextPrint(a,b); break;
                case 'u':
                    p = new UnifiedPrint(a,b); break;
                default:
                    p = new NormalPrint(a,b);
            }
            p.print_header(filea,fileb);
            p.print_script(script);
        }
    }
    
}
