// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.common.gpb;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.jeb.common.gpb.converter.NullConverter;
import ca.jeb.common.infra.JException;
import ca.jeb.common.infra.JGPBAnnotationException;
import ca.jeb.common.infra.JReflectionUtils;
import ca.jeb.common.infra.JStringUtils;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessage.Builder;

/**
 * Base class that generates ProtoBuf classes based on annotations.
 * Inherit from this class to get use of the toProtoBuf().
 */
public class ProtoBufSerializer<G extends GeneratedMessage> implements IProtoBufGenerator<G>
{
  private Class<G>                                                           classType;

  private static final Map<String, Map<Field, ProtoBufAttribute>>            CLASS_TO_FIELD_MAP_CACHE = new HashMap<>();

  private static final Map<Class<? extends Object>, Class<? extends Object>> GPB_JAVA_MAPPING         = new HashMap<>();

  private static final Logger                                                LOGGER                   = LoggerFactory
                                                                                                              .getLogger(ProtoBufSerializer.class);

  static
  {
    // GPB to Java mapping
    GPB_JAVA_MAPPING.put(Integer.class, Integer.TYPE);
    GPB_JAVA_MAPPING.put(Boolean.class, Boolean.TYPE);
    GPB_JAVA_MAPPING.put(Double.class, Double.TYPE);
    GPB_JAVA_MAPPING.put(Long.class, Long.TYPE);
    // Java to GPB mapping
    GPB_JAVA_MAPPING.put(Integer.TYPE, Integer.class);
    GPB_JAVA_MAPPING.put(Boolean.TYPE, Boolean.class);
    GPB_JAVA_MAPPING.put(Double.TYPE, Double.class);
    GPB_JAVA_MAPPING.put(Long.TYPE, Long.class);
  };

  /**
   * Serializes <i>this</i> object into the specified ProtoBuf object.
   * 
   * @return G - Generic extends GeneratedMessage
   * @throws CMSCoreException
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public G toProtoBuf() throws JException
  {
    try
    {
      final Class<? extends Object> fromClazz = this.getClass();
      final Class<? extends GeneratedMessage> protoClazz = getProtoClass(fromClazz);
      if (protoClazz == null)
      {
        throw new JGPBAnnotationException("Class " + fromClazz + " is extending " + this.getClass().getSimpleName()
                + " but was not annotated with ProtoBufEntity annotation.");
      }
      final Method method = protoClazz.getMethod("newBuilder");
      final G.Builder<Builder> protoObjBuilder = (G.Builder<Builder>)method.invoke(null);

      final Map<Field, ProtoBufAttribute> protoBufFields = getAllProtBufFields(fromClazz);
      if (protoBufFields.isEmpty())
      {
        // LOGGER.warn(fromClazz
        // + " has extended ProtoBufSerializer, but not set up any ProtoBufAttribute annotations, returning null.");
        return null;
      }

      for (Entry<Field, ProtoBufAttribute> entry : protoBufFields.entrySet())
      {
        final Field field = entry.getKey();
        final ProtoBufAttribute gpbAnnotation = entry.getValue();
        final String fieldName = field.getName();
        // LOGGER.debug("fieldName: " + fieldName);

        // 1. Determine validity of value
        Object value = JReflectionUtils.runGetter(field, this);

        if (value == null && gpbAnnotation.required())
        {
          throw new CMSCoreAnnotationException("Required field '" + fieldName + "' is null");
        }
        if (value == null)
        {
          continue;
        }

        // 2. Get a value from the object
        final String configedGetter = gpbAnnotation.pojoGetter();
        if (!configedGetter.equals(JStringUtils.EMPTY))
        {
          if (value.getClass().equals(SybTimestamp.class))
          {
            value = (Date)value;
          }
          final Method gpbMethod = value.getClass().getDeclaredMethod(configedGetter, (Class<?>[])null);
          value = gpbMethod.invoke(value, (Object[])null);
        }

        String setter = "set" + JStringUtils.upperCaseFirst(fieldName);
        if (value instanceof IProtoBufGenerator)
        {
          value = ((IProtoBufGenerator)value).toProtoBuf();
        }
        if (value instanceof Collection)
        {
          value = convertCollectionToProtoBufs((Collection<Object>)value);
          if (((Collection)value).isEmpty())
          {
            continue;
          }
          setter = "addAll" + JStringUtils.upperCaseFirst(fieldName);
        }

        final String configedSetter = gpbAnnotation.protoBufSetter();
        if (!configedSetter.equals(JStringUtils.EMPTY))
        {
          setter = configedSetter;
        }
        // LOGGER.debug("setter: " + setter);

        Class<? extends Object> currentClass = value.getClass();
        Class<? extends Object> gpbClass = currentClass;

        final Class<? extends IProtoBufConverter> converterClazz = gpbAnnotation.converter();
        if (converterClazz != NullConverter.class)
        {
          final IProtoBufConverter protoBufConverter = (IProtoBufConverter)converterClazz.newInstance();
          value = protoBufConverter.convertToProtoBuf(value);
          gpbClass = value.getClass();
          currentClass = gpbClass;
        }

        // Need to convert the argument class from non-primitives to primitives, as ProtoBuf uses these.
        gpbClass = getGBPClass(gpbClass, currentClass);

        final Method gpbMethod = protoObjBuilder.getClass().getDeclaredMethod(setter, gpbClass);
        gpbMethod.invoke(protoObjBuilder, value);
      }
      return (G)protoObjBuilder.build();
    }
    catch (Exception e)
    {
      throw new CMSCoreException("Could not generate ProtoBuf object for " + this.getClass() + ": " + e, e);
    }
  }

  /**
   * Set <i>this</i> classes ProtButAttributes from the provided protoBuf object.
   * 
   * @param protoBuf - G extends GeneratedMessage
   * @throws CMSCoreException
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void fromProtoBuf(G protoBuf) throws CMSCoreException
  {
    final Class<? extends Object> toClazz = this.getClass();
    try
    {
      final Class<? extends GeneratedMessage> protoClazz = protoBuf.getClass();
      final Map<Field, ProtoBufAttribute> protoBufFields = getAllProtBufFields(toClazz);
      if (protoBufFields.isEmpty())
      {
        LOGGER.warn("No protoBuf fields have been annotated on the class " + toClazz + ", thus cannot continue.");
        return;
      }

      for (Entry<Field, ProtoBufAttribute> entry : protoBufFields.entrySet())
      {
        final Field field = entry.getKey();
        final ProtoBufAttribute gpbAnnotation = entry.getValue();
        final String fieldName = field.getName();
        final String upperCaseFirstFieldName = JStringUtils.upperCaseFirst(fieldName);
        String setter = "set" + upperCaseFirstFieldName;
        String getter = "get" + upperCaseFirstFieldName;
        String haser = "has" + upperCaseFirstFieldName;
        Boolean isAList = Boolean.FALSE;

        if (!gpbAnnotation.pojoSetter().equals(JStringUtils.EMPTY))
        {
          setter = gpbAnnotation.pojoSetter();
        }
        if (!gpbAnnotation.protoBufGetter().equals(JStringUtils.EMPTY))
        {
          getter = gpbAnnotation.protoBufGetter();
          haser = StringUtils.replaceOnce(getter, "get", "has");
        }
        if (Collection.class.isAssignableFrom(field.getType()))
        {
          getter += "List";
          isAList = Boolean.TRUE;
        }

        final ProtoBufEntity protoBufEntityAnno = field.getType().getAnnotation(ProtoBufEntity.class);
        final Class<? extends IProtoBufConverter> fromProtoBufConverter = gpbAnnotation.converter();

        if (!isAList)
        {
          // no need to continue if this field in the protoBuf is not set
          final boolean hasFieldSet = (boolean)JReflectionUtils.runMethod(protoBuf, haser, (Object[])null);
          if (!hasFieldSet)
          {
            continue;
          }
        }

        Object protoBufValue = JReflectionUtils.runMethod(protoBuf, getter, (Object[])null);
        if (isAList && ((List)protoBufValue).isEmpty())
        {
          // no need to continue if this is a list and it's empty
          continue;
        }

        if (protoBufValue instanceof GeneratedMessage && protoBufEntityAnno != null)
        {
          final Class<? extends GeneratedMessage> entityProtoClazz = protoBufEntityAnno.value();
          final Class<IProtoBufGenerator<G>> pojoClazz = (Class<IProtoBufGenerator<G>>)field.getType();
          final IProtoBufGenerator<G> pojo = pojoClazz.newInstance();

          pojo.fromProtoBuf((G)protoBufValue);

          protoBufValue = pojo;
        }
        if (protoBufValue instanceof Collection)
        {
          protoBufValue = convertCollectionFromProtoBufs((Collection<Object>)protoBufValue, field);
          if (((Collection)protoBufValue).isEmpty())
          {
            continue;
          }
        }

        /**
         * convertCollectionFromProtoBufs() above returns an ArrayList, and we may have a converter to convert to a Set,
         * so we are performing the conversion there
         */

        if (fromProtoBufConverter != NullConverter.class)
        {
          final IProtoBufConverter converter = fromProtoBufConverter.newInstance();
          protoBufValue = converter.convertFromProtoBuf(protoBufValue);
        }

        Class<? extends Object> argClazz = gpbAnnotation.pojoSetterArgClass();
        if (argClazz == NullClass.class)
        {
          argClazz = null;
        }

        JReflectionUtils.runSetter(this, setter, protoBufValue, argClazz);
      }
    }
    catch (Exception e)
    {
      throw new CMSCoreException(
              "Could not generate object of type " + toClazz + " from ProtoBuf object " + protoBuf.getClass() + ": " + e, e);
    }
  }

  @SuppressWarnings("unchecked")
  private Object convertCollectionToProtoBufs(Collection<Object> value) throws CMSCoreException
  {
    if (value.isEmpty())
    {
      return value;
    }
    final Object first = value.toArray()[0];
    if (!(first instanceof IProtoBufGenerator))
    {
      return value;
    }

    final Collection<Object> newCollectionValues;

    /**
     * Maintain the Collection type of value at this stage (if it is a Set), and if conversion is required to a
     * different Collection type, that will be handled by a converter later on
     */
    if (value instanceof Set)
    {
      newCollectionValues = new HashSet<>();
    }
    else
    {
      newCollectionValues = new ArrayList<>();
    }

    for (Object iProtoBufGenObj : value)
    {
      final IProtoBufGenerator<GeneratedMessage> iProtoBufGen = (IProtoBufGenerator<GeneratedMessage>)iProtoBufGenObj;
      newCollectionValues.add(iProtoBufGen.toProtoBuf());
    }

    return newCollectionValues;
  }

  @SuppressWarnings("unchecked")
  private Object convertCollectionFromProtoBufs(Collection<Object> value, Field field) throws CMSCoreException, InstantiationException,
          IllegalAccessException
  {
    if (value.isEmpty())
    {
      return value;
    }

    final ParameterizedType listType = (ParameterizedType)field.getGenericType();
    final Class<IProtoBufGenerator<G>> pojoClazz = (Class<IProtoBufGenerator<G>>)listType.getActualTypeArguments()[0];
    final ProtoBufEntity protoBufEntityAnno = pojoClazz.getAnnotation(ProtoBufEntity.class);

    final Object first = value.toArray()[0];
    if (!(first instanceof GeneratedMessage) && protoBufEntityAnno == null)
    {
      return value;
    }

    final Collection<Object> newListvalues = new ArrayList<>();
    for (Object protoBufValue : value)
    {

      final IProtoBufGenerator<G> pojo = pojoClazz.newInstance();

      pojo.fromProtoBuf((G)protoBufValue);

      newListvalues.add(pojo);
    }

    return newListvalues;
  }

  private Map<Field, ProtoBufAttribute> getAllProtBufFields(Class<? extends Object> fromClazz)
  {
    Map<Field, ProtoBufAttribute> protoBufFields = CLASS_TO_FIELD_MAP_CACHE.get(fromClazz.getCanonicalName());
    if (protoBufFields != null)
    {
      return protoBufFields;
    }

    protoBufFields = new HashMap<>();
    final List<Field> fields = JReflectionUtils.getAllFields(new ArrayList<Field>(), fromClazz);

    for (Field field : fields)
    {
      final Annotation annotation = field.getAnnotation(ProtoBufAttribute.class);
      if (annotation == null)
      {
        continue;
      }
      final ProtoBufAttribute gpbAnnotation = (ProtoBufAttribute)annotation;
      protoBufFields.put(field, gpbAnnotation);
    }

    synchronized (CLASS_TO_FIELD_MAP_CACHE)
    {
      CLASS_TO_FIELD_MAP_CACHE.put(fromClazz.getCanonicalName(), protoBufFields);
    }

    return protoBufFields;
  }

  private Class<? extends GeneratedMessage> getProtoClass(Class<? extends Object> clazz)
  {
    final ProtoBufEntity annotation = clazz.getAnnotation(ProtoBufEntity.class);
    final Class<? extends GeneratedMessage> gpbClazz = annotation.value();
    if (gpbClazz == null)
    {
      return null;
    }
    return gpbClazz;
  }

  private Class<? extends Object> getGBPClass(Class<? extends Object> gpbClass, Class<? extends Object> currentClass)
  {
    final Class<? extends Object> mappedType = GPB_JAVA_MAPPING.get(currentClass);
    if (mappedType != null)
    {
      return mappedType;
    }
    if (currentClass.equals(Integer.class))
    {
      return Integer.TYPE;
    }
    if (currentClass.equals(Boolean.class))
    {
      return Boolean.TYPE;
    }
    if (currentClass.equals(Double.class))
    {
      return Double.TYPE;
    }
    if (currentClass.equals(Long.class) || currentClass.equals(Date.class) || currentClass.equals(SybTimestamp.class))
    {
      return Long.TYPE;
    }
    // A TOTAL hack, for some reason Hibernate uses java.util.Arrays$ArrayList, as opposed to java.util.ArrayList
    final String currentClassSimpleName = currentClass.toString();
    if (currentClass.equals(ArrayList.class) || currentClassSimpleName.contains("Arrays$ArrayList"))
    {
      return Iterable.class;
    }
    return gpbClass;
  }

  /**
   * @return the type
   */
  public Class<? extends GeneratedMessage> getClassType()
  {
    return classType;
  }

  /**
   * @param classType the classType to set
   */
  public void setClassType(Class<G> classType)
  {
    this.classType = classType;
  }
}