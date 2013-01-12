package com.jpexs.flashplayer;

import com.docuverse.swt.flash.FlashPlayer;
import java.awt.Canvas;
import java.awt.Dimension;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class FlashPanel extends Canvas {

   private Thread swtThread;
   private Shell shell;
   private FlashPlayer player;

   public FlashPanel(int width, int height) {
      setPreferredSize(new Dimension(width, height));
   }

   public void dispose() {
      if (player != null) {

         player.getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
               shell.dispose();
            }
         });
      }
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
                     player = new FlashPlayer(shell, SWT.NONE);
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
         while (this.player == null) {
            try {
               this.swtThread.wait(100);
            } catch (InterruptedException e) {
               this.player = null;
               this.swtThread = null;
               break;
            }
         }
      }
      setSize(getWidth() - 1, getHeight() - 1);
      setSize(getWidth() + 1, getHeight() + 1);
   }

   public void displaySWF(final String swf) {
      dispose();
      startDisplay();
      player.getDisplay().asyncExec(new Runnable() {
         @Override
         public void run() {
            player.setMovie(swf);
            player.activate();
         }
      });


   }

   /**
    * Stops the swt background thread.
    */
   private void disconnect() {
      if (swtThread != null) {
         player = null;
         swtThread.interrupt();
         swtThread = null;
      }
   }
}