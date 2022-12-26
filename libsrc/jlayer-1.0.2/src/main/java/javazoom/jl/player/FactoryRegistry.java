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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import javazoom.jl.decoder.JavaLayerException;


/**
 * The <code>FactoryRegistry</code> class stores the factories
 * for all the audio device implementations available in the system.
 * <p>
 * Instances of this class are thread-safe.
 *
 * @author Mat McGowan
 * @since 0.0.8
 * @see "META-INF/services/javazoom.jl.player.AudioDeviceFactory"
 */
public class FactoryRegistry {

    private static final Logger logger = Logger.getLogger(FactoryRegistry.class.getName());

    private static FactoryRegistry instance = null;

    public static synchronized FactoryRegistry systemRegistry() {
        if (instance == null) {
            instance = new FactoryRegistry();
            instance.registerDefaultFactories();
        }
        return instance;
    }

    protected final Map<Class<? extends AudioDeviceFactory>, AudioDeviceFactory> factories = new HashMap<>();

    /**
     * Registers an <code>AudioDeviceFactory</code> instance
     * with this registry.
     */
    public void addFactory(AudioDeviceFactory factory) {
        factories.put(factory.getClass(), factory);
    }

    public void removeFactoryType(Class<?> cls) {
        factories.remove(cls);
    }

    public void removeFactory(AudioDeviceFactory factory) {
        factories.remove(factory.getClass());
    }

    /**
     * specify a factory
     * @since 1.0.2
     * @throws NullPointerException if not registered class is specified.
     */
    public AudioDevice createAudioDevice(Class<? extends AudioDeviceFactory> clazz) throws JavaLayerException {
        return factories.get(clazz).createAudioDevice();
    }

    /**
     * @since 1.0.2 selecting factory depends on {@link AudioDeviceFactory#priority()}
     * @throws JavaLayerException not found or others
     */
    public AudioDevice createAudioDevice() throws JavaLayerException {
        AudioDevice device = null;

        JavaLayerException lastEx = null;
logger.fine("factories order: " + Arrays.toString(getFactoriesPriority()));
        for (AudioDeviceFactory factory : getFactoriesPriority()) {
            try {
                device = factory.createAudioDevice();
                break;
            } catch (JavaLayerException ex) {
                lastEx = ex;
            }
        }

        if (device == null && lastEx != null) {
            throw new JavaLayerException("Cannot create AudioDevice", lastEx);
        }

        return device;
    }

    /**
     * @since 1.0.2 order of factories depends on {@link AudioDeviceFactory#priority()}
     */
    protected AudioDeviceFactory[] getFactoriesPriority() {
        synchronized (factories) {
            return factories.values().stream().sorted((o1, o2) -> o2.priority() - o1.priority()).toArray(AudioDeviceFactory[]::new);
        }
    }

    /**
     * @since 1.0.2 this uses the service loader
     */
    protected void registerDefaultFactories() {
        ServiceLoader<AudioDeviceFactory> loader = ServiceLoader.load(AudioDeviceFactory.class);
        for (AudioDeviceFactory factory : loader) {
logger.fine("initial factory: " + factory);
            addFactory(factory);
        }
    }
}
