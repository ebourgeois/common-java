// Copyright (c) 2014 Erick Bourgeois, All Rights Reserved

package ca.jeb.common.gpb;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.jeb.common.gpb.converter.NullConverter;
import ca.jeb.common.infra.JException;
import ca.jeb.common.infra.JGPBAnnotationException;
import ca.jeb.common.infra.JReflectionUtils;
import ca.jeb.common.infra.JStringUtils;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage;

/**
 * @author <a href="mailto:erick@jeb.ca">Erick Bourgeois</a>
 */
public class ProtobufSerialization<G extends GeneratedMessage, P extends Object> implements IProtoBufGenerator<G, P>
{
  private static final Map<Class<? extends Object>, Class<? extends Object>> GPB_JAVA_MAPPING         = new ConcurrentHashMap<>();
  private static final Map<String, Map<Field, ProtoBufAttribute>>            CLASS_TO_FIELD_MAP_CACHE = new ConcurrentHashMap<>();

  private static final Logger                                                LOGGER                   = LoggerFactory
                                                                                                              .getLogger(ProtoBufSerializer.class);

  /**
   * @see ca.jeb.common.gpb.IProtoBufGenerator#toProtoBuf(java.lang.Object)
   */
  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public G toProtoBuf(P pojo) throws JException
  {
    try
    {
      final Class<P> fromClazz = (Class<P>)pojo.getClass();
      final Class<G> protoClazz = getProtoClass(fromClazz);
      if (protoClazz == null)
      {
        throw new JGPBAnnotationException("Can not serialize a " + fromClazz + " without the ProtoBufEntity annotation on " + fromClazz);
      }
      final Method method = protoClazz.getMethod("newBuilder");
      final G.Builder protoObjBuilder = (G.Builder)method.invoke(null);

      Map<FieldDescriptor, Object> fields = protoObjBuilder.getAllFields();
      final Map<Field, ProtoBufAttribute> protoBufFields = getAllProtbufFields(fromClazz);
      if (protoBufFields.isEmpty())
      {
        return null;
      }

      for (Entry<Field, ProtoBufAttribute> entry : protoBufFields.entrySet())
      {
        final Field field = entry.getKey();
        final ProtoBufAttribute gpbAnnotation = entry.getValue();
        final String fieldName = field.getName();

        // 1. Determine validity of value
        Object value = JReflectionUtils.runGetter(pojo, field);

        if (value == null && gpbAnnotation.required())
        {
          throw new JGPBAnnotationException("Required field '" + fieldName + "' is null");
        }

        // If value is null and it is not required, skip, as the default for Protobuf values is null
        if (value == null)
        {
          continue;
        }

        // 2. Call recursively if this is a ProtobufEntity
        value = serializeProtobufEntity(value);

        String setter = "set" + JStringUtils.upperCaseFirst(fieldName);
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
        gpbClass = getGBPClass(value, gpbClass);

        final Method gpbMethod = protoObjBuilder.getClass().getDeclaredMethod(setter, gpbClass);
        gpbMethod.invoke(protoObjBuilder, value);
      }
      return (G)protoObjBuilder.build();

    }
    catch (Exception e)
    {
      throw new JException("Could not generate ProtoBuf object for " + this.getClass() + ": " + e, e);
    }
  }

  /**
   * @see ca.jeb.common.gpb.IProtoBufGenerator#fromProtoBuf(com.google.protobuf.GeneratedMessage)
   */
  @Override
  public P fromProtoBuf(G protoBuf) throws JException
  {
    return null;
  }

  /**
   * Return the Protobuf Class based on the pojo class, i.e. grab the value from the ProtoBufEntity annotation.
   * 
   * @param clazz
   * @return Class
   */
  @SuppressWarnings("unchecked")
  private Class<G> getProtoClass(Class<P> clazz)
  {
    final ProtoBufEntity annotation = clazz.getAnnotation(ProtoBufEntity.class);
    final Class<G> gpbClazz = (Class<G>)annotation.value();
    if (gpbClazz == null)
    {
      return null;
    }
    return gpbClazz;
  }

  /**
   * @param fromClazz
   * @return
   */
  private Map<Field, ProtoBufAttribute> getAllProtbufFields(Class<? extends Object> fromClazz)
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

  private boolean isProtbufEntity(Object object)
  {
    final ProtoBufEntity protoBufEntity = object.getClass().getAnnotation(ProtoBufEntity.class);

    if (protoBufEntity != null)
    {
      return true;
    }
    return false;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Object serializeProtobufEntity(Object attributeValue) throws JException
  {
    final ProtoBufEntity protoBufEntity = attributeValue.getClass().getAnnotation(ProtoBufEntity.class);

    if (protoBufEntity == null)
    {
      return attributeValue;
    }

    final ProtobufSerialization serializer = new ProtobufSerialization<>();

    return serializer.toProtoBuf(attributeValue);
  }

  private Object convertCollectionToProtoBufs(Collection<Object> value) throws JException
  {
    if (value.isEmpty())
    {
      return value;
    }
    final Object first = value.toArray()[0];
    if (!isProtbufEntity(first))
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
      newCollectionValues.add(serializeProtobufEntity(iProtoBufGenObj));
    }

    return newCollectionValues;
  }

  private Class<? extends Object> getGBPClass(Object value, Class<? extends Object> gpbClass)
  {
    if (value instanceof Integer)
    {
      return Integer.TYPE;
    }
    if (value instanceof Boolean)
    {
      return Boolean.TYPE;
    }
    if (value instanceof Double)
    {
      return Double.TYPE;
    }
    if (value instanceof Long || value instanceof Date)
    {
      return Long.TYPE;
    }
    if (value instanceof List)
    {
      return Iterable.class;
    }
    return gpbClass;
  }
}