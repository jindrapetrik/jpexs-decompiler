/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package javazoom.jl.player;

/**
 * NullAudioDeviceFactory.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022/10/03 nsano initial version <br>
 * @since 1.0.2
 */
public class NullAudioDeviceFactory extends AudioDeviceFactory {

    @Override
    public synchronized AudioDevice createAudioDevice() {
        return new NullAudioDevice();
    }

    @Override
    protected int priority() {
        return 0;
    }
}
