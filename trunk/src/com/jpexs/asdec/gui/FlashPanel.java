package com.jpexs.asdec.gui;

import java.awt.Canvas;
import java.awt.Dimension;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleClientSite;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class FlashPanel extends Canvas {

   private Thread swtThread;
   private OleFrame oleFrame;
   private OleClientSite clientSite;
   private Shell shell;
   private int width;
   private int height;

   public FlashPanel(int width, int height) {
      setPreferredSize(new Dimension(width, height));
      this.width = width;
      this.height = height;
   }

   private void disposeClient() {
      if (clientSite != null) {
         clientSite.dispose();
      }
      clientSite = null;
   }

   public void dispose() {

      oleFrame.getDisplay().asyncExec(new Runnable() {
         @Override
         public void run() {
            shell.dispose();
         }
      });
      disconnect();
   }

   private void startDisplay() {
      if (this.swtThread == null) {
         final Canvas canvas = this;
         this.swtThread = new Thread() {
            @Override
            public void run() {
               try {
                  Display display = new Display();
                  shell = SWT_AWT.new_Shell(display, canvas);
                  shell.setLayout(new FillLayout());

                  synchronized (this) {
                     oleFrame = new OleFrame(shell, SWT.NONE);
                     clientSite = new OleClientSite(oleFrame, SWT.NONE, "Shell.Explorer");

                     this.notifyAll();
                  }

                  shell.open();
                  while (!isInterrupted() && !shell.isDisposed()) {
                     if (!display.readAndDispatch()) {
                        display.sleep();
                     }
                  }
                  shell.dispose();
                  display.dispose();
               } catch (Exception e) {
                  interrupt();
               }
            }
         };
         this.swtThread.start();
      }

      synchronized (this.swtThread) {
         while (this.oleFrame == null) {
            try {
               this.swtThread.wait(100);
            } catch (InterruptedException e) {
               this.oleFrame = null;
               this.swtThread = null;
               break;
            }
         }
      }
      setSize(getWidth() - 1, getHeight() - 1);
      setSize(getWidth() + 1, getHeight() + 1);
   }


   public void displaySWF(String swf) {
      startDisplay();
      final String loadSWF = swf;
      oleFrame.getDisplay().asyncExec(new Runnable() {
         @Override
         public void run() {

            OleAutomation explorer = new OleAutomation(clientSite);
            int[] navigate = explorer.getIDsOfNames(new String[]{"Navigate"});
            if (navigate != null) {
               Variant result = explorer.invoke(navigate[0], new Variant[]{new Variant(loadSWF)});               
               if (result == null) {
                  disposeClient();
               } else {
                  result.dispose();
               }               
            } else {
               disposeClient();
            }           
         }
      });
      

   }

   /**
    * Stops the swt background thread.
    */
   private void disconnect() {
      if (swtThread != null) {
         oleFrame = null;
         swtThread.interrupt();
         swtThread = null;
      }
   }
}