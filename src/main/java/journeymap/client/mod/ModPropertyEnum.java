package journeymap.client.mod;

import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Some mods have custom PropertyEnums with methods useful for deriving colors from Blockstates.
 * This is a reflection-derived wrapper for such PropertyEnums and methods.
 *
 * @param <T> return type of the method invoked on the PropertyEnum
 */
@SuppressWarnings("unchecked")
@ParametersAreNonnullByDefault
public class ModPropertyEnum<T> {
    private static final Logger logger = Journeymap.getLogger();
    private final boolean valid;
    private final PropertyEnum propertyEnum;
    private final Method method;

    /**
     * Constructor.
     *
     * @param propertyEnum PropertyEnum instance.
     * @param method       A method used with the PropertyEnum value
     * @param returnType   The return type <T> of the method
     */
    public ModPropertyEnum(PropertyEnum propertyEnum, Method method, Class<T> returnType) {
        this.valid = propertyEnum != null && method != null;
        this.propertyEnum = propertyEnum;
        this.method = method;
    }

    /**
     * Constructor.
     *
     * @param propertyEnum PropertyEnum instance.
     * @param methodName   Named method on the PropertyEnum value
     * @param returnType   The return type <T> of the method
     */
    public ModPropertyEnum(PropertyEnum propertyEnum, String methodName, Class<T> returnType, Class<?>[] methodArgTypes) {
        this(propertyEnum, lookupMethod(propertyEnum, methodName, methodArgTypes), returnType);
    }

    /**
     * Constructor.
     *
     * @param declaringClassName          class where the PropertyEnum instance is declared. ("net.minecraft.block.BlockDirectional")
     * @param propertyEnumStaticFieldName static field name of the PropertyEnum declared on the class. ("FACING")
     * @param methodName                  method name on the PropertyEnum class. ("getAllowedValues")
     */
    public ModPropertyEnum(String declaringClassName, String propertyEnumStaticFieldName, String methodName, Class<T> returnType) {
        this(declaringClassName, propertyEnumStaticFieldName, methodName, returnType, new Class[0]);
    }

    /**
     * Constructor.
     *
     * @param declaringClassName          class where the PropertyEnum instance is declared. ("net.minecraft.block.BlockDirectional")
     * @param propertyEnumStaticFieldName static field name of the PropertyEnum declared on the class. ("FACING")
     * @param methodName                  method name on the PropertyEnum class. ("getAllowedValues")
     * @param methodArgTypes              (optional) arguments passed to the method
     */
    public ModPropertyEnum(String declaringClassName, String propertyEnumStaticFieldName, String methodName, Class<T> returnType, Class<?>[] methodArgTypes) {
        this(lookupPropertyEnum(declaringClassName, propertyEnumStaticFieldName), methodName, returnType, methodArgTypes);
    }

    /**
     * Constructor.
     *
     * @param declaringClassName          class where the PropertyEnum instance is declared. ("net.minecraft.block.BlockDirectional")
     * @param propertyEnumStaticFieldName static field name of the PropertyEnum declared on the class. ("FACING")
     * @param method                      a method used with the PropertyEnum value
     */
    public ModPropertyEnum(String declaringClassName, String propertyEnumStaticFieldName, Method method, Class<T> returnType) {
        this(lookupPropertyEnum(declaringClassName, propertyEnumStaticFieldName), method, returnType);
    }

    /**
     * Find a declared PropertyEnum on a named class via a static declaration.
     *
     * @param declaringClassName          class where the PropertyEnum instance is declared. ("net.minecraft.block.BlockDirectional")
     * @param propertyEnumStaticFieldName static field name of the PropertyEnum declared on the class. ("FACING")
     * @return the PropertyEnum field or null if not found
     */
    public static PropertyEnum lookupPropertyEnum(String declaringClassName, String propertyEnumStaticFieldName) {
        try {
            Class declaringClass = Class.forName(declaringClassName);
            return (PropertyEnum) ReflectionHelper.findField(declaringClass, propertyEnumStaticFieldName).get(declaringClass);
        } catch (Exception e) {
            Journeymap.getLogger().error("Error reflecting PropertyEnum on %s.%s: %s", declaringClassName,
                    propertyEnumStaticFieldName, LogFormatter.toPartialString(e));
        }
        return null;
    }

    /**
     * Find a method on a PropertyEnum by name and optional arguments.
     *
     * @param propertyEnum   the PropertyEnum
     * @param methodName     the method name
     * @param methodArgTypes optional method arguments
     * @return method or null if not found
     */
    public static Method lookupMethod(PropertyEnum propertyEnum, String methodName, Class... methodArgTypes) {
        if (propertyEnum != null) {
            return lookupMethod(propertyEnum.getValueClass().getName(), methodName, methodArgTypes);
        }
        return null;
    }

    /**
     * Find a method on an object by classname, methodname, and optional arguments.
     *
     * @param declaringClassName the PropertyEnum
     * @param methodName         the method name
     * @param methodArgTypes     optional method arguments
     * @return method or null if not found
     */
    public static Method lookupMethod(String declaringClassName, String methodName, Class... methodArgTypes) {
        try {
            Class declaringClass = Class.forName(declaringClassName);
            return ReflectionHelper.findMethod(declaringClass, methodName, null, methodArgTypes);
        } catch (Exception e) {
            Journeymap.getLogger().error("Error reflecting method %s.%s(): %s", declaringClassName,
                    methodName, LogFormatter.toPartialString(e));
        }
        return null;
    }

    /**
     * Gets the PropertyEnum found via reflection.
     *
     * @return PropertyEnum
     */
    public PropertyEnum getPropertyEnum() {
        return propertyEnum;
    }

    /**
     * Whether the PropertyEnum and method were successfully reflected.
     *
     * @return true if valid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * If the BlockState has the PropertyEnum as a property,
     * this will get the enum value and then call the method on that value
     * in order to return a <T>.
     *
     * @param blockState the blockstate to check
     * @param args       optional args which need to be passed to the method
     * @return a <T> or null if the property isn't on the blockState or an error is produced.
     */
    @Nullable
    public T getValue(IBlockState blockState, @Nullable Object... args) {
        if (valid) {
            try {
                Comparable<?> enumValue = blockState.getProperties().get(propertyEnum);
                if (enumValue != null) {
                    return (T) method.invoke(enumValue, args);
                }
            } catch (Exception e) {
                logger.error("Error using mod PropertyEnum: " + LogFormatter.toPartialString(e));
            }
        }
        return null;
    }

    /**
     * Check Blockstate with a collection of ModPropertyEnums, returning the first non-null value.
     *
     * @param modPropertyEnums collection
     * @param blockState       the blockstate to check
     * @param args             optional args which need to be passed to the method
     * @param <T>              return type
     * @return null if none of the ModPropertyEnums return a value.
     */
    @Nullable
    public static <T> T getFirstValue(Collection<ModPropertyEnum<T>> modPropertyEnums, IBlockState blockState, @Nullable Object... args) {
        for (ModPropertyEnum<T> modPropertyEnum : modPropertyEnums) {
            T result = modPropertyEnum.getValue(blockState, args);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
