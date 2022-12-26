/*
 * 11/19/04        1.0 moved to LGPL.
 * 29/01/00        Initial version. mdm@techie.com
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package javazoom.jl.player;

import javazoom.jl.decoder.JavaLayerException;


/**
 * This class is responsible for creating instances of the
 * JavaSoundAudioDevice. The audio device implementation is loaded
 * and tested dynamically as not all systems will have support
 * for JavaSound, or they may have the incorrect version.
 */
public class JavaSoundAudioDeviceFactory extends AudioDeviceFactory {

    private boolean tested = false;

    @Override
    public synchronized AudioDevice createAudioDevice()
            throws JavaLayerException {

        if (!tested) {
            testAudioDevice();
            tested = true;
        }

        return new JavaSoundAudioDevice();
    }

    private void testAudioDevice() throws JavaLayerException {
        JavaSoundAudioDevice dev = new JavaSoundAudioDevice();
        dev.test();
    }

    @Override
    protected int priority() {
        return 90;
    }
}
