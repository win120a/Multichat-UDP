package ac.adproj.mchat.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Common factory of services that use SPI mechanism (ServiceLoader).
 *
 * @author Andy Cheung
 */
public final class CommonSpiFactory {
    private CommonSpiFactory() {
        throw new AssertionError("No instance of CommonSpiFactory for you! ");
    }

    public static final Logger LOG = LoggerFactory.getLogger(CommonSpiFactory.class);

    public static <T> T getServiceImplementation(Class<T> serviceType, Class<? extends T> nullImplementationClass) {
        ServiceLoader<T> sl = ServiceLoader.load(serviceType);

        long count = sl.stream().count();

        if (count > 1) {
            LOG.warn("More than one service implementation detected. Only first implementation will be used. Service class: {}",
                    serviceType.getName());
        } else if (count == 0) {
            LOG.warn("No authentication service detected! Null implementation ({}) will be used. Service class: {}",
                    nullImplementationClass.getName(),
                    serviceType.getName());

            try {
                Constructor<? extends T> nullImplCtor = nullImplementationClass.getConstructor();
                return nullImplCtor.newInstance();
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new ServiceConfigurationError("Failed to invoke no-arg constructor in null implementation.", e);
            }
        }

        return sl.iterator().next();
    }
}
