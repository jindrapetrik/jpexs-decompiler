package com.jpexs.flashplayer;

import com.docuverse.swt.flash.FlashPlayer;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class FlashPanel extends Canvas {

   private Thread swtThread;
   private FlashPlayer swtPlayer;

   /**
    * Connect this canvas to a SWT shell with a Browser component and starts a
    * background thread to handle SWT events. This method waits until the
    * browser component is ready.
    */
   private void connect() {
      if (this.swtThread == null) {
         final Canvas canvas = this;
         this.swtThread = new Thread() {
            @Override
            public void run() {
               try {
                  Display display = new Display();
                  Shell shell = SWT_AWT.new_Shell(display, canvas);
                  shell.setLayout(new FillLayout());                  

                  synchronized (this) {
                     swtPlayer = new FlashPlayer(shell, SWT.NONE);
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

      // Wait for the Browser instance to become ready
      synchronized (this.swtThread) {
         while (this.swtPlayer == null) {
            try {
               this.swtThread.wait(100);
            } catch (InterruptedException e) {
               this.swtPlayer = null;
               this.swtThread = null;
               break;
            }
         }
      }
   }

   /**
    * Stops the swt background thread.
    */
   private void disconnect() {
      if (swtThread != null) {
         swtPlayer = null;
         swtThread.interrupt();
         swtThread = null;
      }
   }

   /**
    * Ensures that the SWT background thread is stopped if this canvas is
    * removed from it's parent component (e.g. because the frame has been
    * disposed).
    */
   @Override
   public void removeNotify() {
      super.removeNotify();
      disconnect();
   }

   @Override
   public void addNotify() {
      super.addNotify();
      connect();
   }

   public void displaySWF(final String swf) {
      swtPlayer.getDisplay().asyncExec(new Runnable() {
         @Override
         public void run() {
            swtPlayer.setMovie(swf);
            swtPlayer.activate();
         }
      });
      setSize(getWidth() - 1, getHeight() - 1);
      setSize(getWidth() + 1, getHeight() + 1);
   }
}