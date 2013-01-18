package com.jpexs.flashplayer;

import com.docuverse.swt.flash.FlashPlayer;
import java.awt.Canvas;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class FlashPanel extends Canvas {
   
   private Thread swtThread;
   private FlashPlayer swtPlayer;
   private boolean noFlash = false;
   private Shell shell;

   /**
    * Connect this canvas to a SWT shell with a Browser component and starts a
    * background thread to handle SWT events. This method waits until the
    * browser component is ready.
    */
   private void connect() {
      if (noFlash) {
         return;
      }
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
               } catch (SWTError e) {
                  e.printStackTrace();
                  noFlash = true;
                  interrupt();
               } catch (Exception e) {
                  e.printStackTrace();
                  interrupt();
               } catch (NoClassDefFoundError e) {
                  e.printStackTrace();
                  noFlash = true;
                  interrupt();
               } catch (UnsatisfiedLinkError e) {
                  e.printStackTrace();
                  noFlash = true;
                  interrupt();
               }
            }
         };
         this.swtThread.start();
      }

      // Wait for the Browser instance to become ready
      synchronized (this.swtThread) {
         while (this.swtPlayer == null) {
            if (noFlash) {
               break;
            }
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
      if (noFlash) {
         return;
      }
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
      if (noFlash) {
         return;
      }
      disconnect();
   }

   @Override
   public void addNotify() {
      super.addNotify();
      if (noFlash) {
         return;
      }
      connect();
   }

   private void refresh() {
      setSize(getWidth() - 1, getHeight() - 1);
      new Timer().schedule(new TimerTask() {
         public void run() {
            setSize(getWidth() + 1, getHeight() + 1);
         }
      }, 100);

   }

   public void displaySWF(final String swf) {
      if (noFlash) {
         return;
      }
      swtPlayer.getDisplay().syncExec(new Runnable() {
         @Override
         public void run() {
            swtPlayer.setMovie(swf);
            swtPlayer.activate();
         }
      });
      refresh();
   }
}