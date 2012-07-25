/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.json;

import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.Data;
import com.google.api.client.util.FieldInfo;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Types;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Abstract low-level JSON parser.
 *
 * <p>
 * Implementation has no fields and therefore thread-safe, but sub-classes are not necessarily
 * thread-safe.
 * </p>
 *
 * <p>
 * Backwards incompatibility warning: 1.9 introduced {@link #getUnsignedIntegerValue} and
 * {@link #getUnsignedLongValue} to this abstract class.
 * </p>
 *
 * @since 1.3
 * @author Yaniv Inbar
 */
public abstract class JsonParser {

  /** Returns the JSON factory from which this generator was created. */
  public abstract JsonFactory getFactory();

  /**
   * Closes the parser and the underlying input stream or reader, and releases any memory associated
   * with it.
   *
   * @throws IOException if failed
   */
  public abstract void close() throws IOException;

  /** Returns the next token from the stream or {@code null} to indicate end of input. */
  public abstract JsonToken nextToken() throws IOException;

  /**
   * Returns the token the parser currently points to or {@code null} for none (at start of input or
   * after end of input).
   */
  public abstract JsonToken getCurrentToken();

  /**
   * Returns the most recent field name or {@code null} for array values or for root-level values.
   */
  public abstract String getCurrentName() throws IOException;

  /**
   * Skips to the matching {@link JsonToken#END_ARRAY} if current token is
   * {@link JsonToken#START_ARRAY}, the matching {@link JsonToken#END_OBJECT} if the current token
   * is {@link JsonToken#START_OBJECT}, else does nothing.
   */
  public abstract JsonParser skipChildren() throws IOException;

  /**
   * Returns a textual representation of the current token or {@code null} if
   * {@link #getCurrentToken()} is {@code null}.
   */
  public abstract String getText() throws IOException;

  // TODO(yanivi): Jackson provides getTextCharacters(), getTextLength(), and getTextOffset()

  /** Returns the byte value of the current token. */
  public abstract byte getByteValue() throws IOException;

  /** Returns the short value of the current token. */
  public abstract short getShortValue() throws IOException;

  /** Returns the int value of the current token. */
  public abstract int getIntValue() throws IOException;

  /** Returns the float value of the current token. */
  public abstract float getFloatValue() throws IOException;

  /** Returns the long value of the current token. */
  public abstract long getLongValue() throws IOException;

  /** Returns the double value of the current token. */
  public abstract double getDoubleValue() throws IOException;

  /** Returns the {@link BigInteger} value of the current token. */
  public abstract BigInteger getBigIntegerValue() throws IOException;

  /**
   * Returns the {@link UnsignedInteger} value of the current token.
   *
   * @since 1.9
   */
  public abstract UnsignedInteger getUnsignedIntegerValue() throws IOException;

  /**
   * Returns the {@link UnsignedLong} value of the current token.
   *
   * @since 1.9
   */
  public abstract UnsignedLong getUnsignedLongValue() throws IOException;

  /** Returns the {@link BigDecimal} value of the current token. */
  public abstract BigDecimal getDecimalValue() throws IOException;

  /**
   * Parse a JSON object, array, or value into a new instance of the given destination class using
   * {@link JsonParser#parse(Class, CustomizeJsonParser)}, and then closes the parser.
   *
   * @param <T> destination class type
   * @param destinationClass destination class that has a public default constructor to use to
   *        create a new instance
   * @param customizeParser optional parser customizer or {@code null} for none
   * @return new instance of the parsed destination class
   */
  public final <T> T parseAndClose(Class<T> destinationClass, CustomizeJsonParser customizeParser)
      throws IOException {
    try {
      return parse(destinationClass, customizeParser);
    } finally {
      close();
    }
  }

  /**
   * Skips the values of all keys in the current object until it finds the given key.
   * <p>
   * Before this method is called, the parser must either point to the start or end of a JSON object
   * or to a field name. After this method ends, the current token will either be the
   * {@link JsonToken#END_OBJECT} of the current object if the key is not found, or the value of the
   * key that was found.
   * </p>
   *
   * @param keyToFind key to find
   */
  public final void skipToKey(String keyToFind) throws IOException {
    skipToKey(Collections.singleton(keyToFind));
  }

  /**
   * Skips the values of all keys in the current object until it finds one of the given keys.
   * <p>
   * Before this method is called, the parser must either point to the start or end of a JSON object
   * or to a field name. After this method ends, the current token will either be the
   * {@link JsonToken#END_OBJECT} of the current object if no matching key is found, or the value
   * of the key that was found.
   * </p>
   *
   * @param keysToFind set of keys to look for
   * @return name of the first matching key found or {@code null} if no match was found
   * @since 1.10
   */
  public final String skipToKey(Set<String> keysToFind) throws IOException {
    JsonToken curToken = startParsingObjectOrArray();
    while (curToken == JsonToken.FIELD_NAME) {
      String key = getText();
      nextToken();
      if (keysToFind.contains(key)) {
        return key;
      }
      skipChildren();
      curToken = nextToken();
    }
    return null;
  }

  /** Starts parsing that handles start of input by calling {@link #nextToken()}. */
  private JsonToken startParsing() throws IOException {
    JsonToken currentToken = getCurrentToken();
    // token is null at start, so get next token
    if (currentToken == null) {
      currentToken = nextToken();
    }
    Preconditions.checkArgument(currentToken != null, "no JSON input found");
    return currentToken;
  }

  /**
   * Starts parsing an object or array by making sure the parser points to an object field name,
   * first array value or end of object or array.
   * <p>
   * If the parser is at the start of input, {@link #nextToken()} is called. The current token must
   * then be {@link JsonToken#START_OBJECT}, {@link JsonToken#END_OBJECT},
   * {@link JsonToken#START_ARRAY}, {@link JsonToken#END_ARRAY}, or {@link JsonToken#FIELD_NAME}.
   * For an object only, after the method is called, the current token must be either
   * {@link JsonToken#FIELD_NAME} or {@link JsonToken#END_OBJECT}.
   * </p>
   */
  private JsonToken startParsingObjectOrArray() throws IOException {
    JsonToken currentToken = startParsing();
    switch (currentToken) {
      case START_OBJECT:
        currentToken = nextToken();
        Preconditions.checkArgument(
            currentToken == JsonToken.FIELD_NAME || currentToken == JsonToken.END_OBJECT,
            currentToken);
        break;
      case START_ARRAY:
        currentToken = nextToken();
        break;
    }
    return currentToken;
  }

  /**
   * Parse a JSON Object from the given JSON parser -- which is closed after parsing completes --
   * into the given destination object, optionally using the given parser customizer.
   * <p>
   * Before this method is called, the parser must either point to the start or end of a JSON object
   * or to a field name.
   * </p>
   *
   * @param destination destination object
   * @param customizeParser optional parser customizer or {@code null} for none
   */
  public final void parseAndClose(Object destination, CustomizeJsonParser customizeParser)
      throws IOException {
    try {
      parse(destination, customizeParser);
    } finally {
      close();
    }
  }

  /**
   * Parse a JSON object, array, or value into a new instance of the given destination class,
   * optionally using the given parser customizer.
   * <p>
   * If it parses an object, after this method ends, the current token will be the object's ending
   * {@link JsonToken#END_OBJECT}. If it parses an array, after this method ends, the current token
   * will be the array's ending {@link JsonToken#END_ARRAY}.
   * </p>
   *
   * @param <T> destination class type
   * @param destinationClass destination class that has a public default constructor to use to
   *        create a new instance
   * @param customizeParser optional parser customizer or {@code null} for none
   * @return new instance of the parsed destination class
   */
  public final <T> T parse(Class<T> destinationClass, CustomizeJsonParser customizeParser)
      throws IOException {
    startParsing();
    @SuppressWarnings("unchecked")
    T result = (T) parse(destinationClass, false, customizeParser);
    return result;
  }

  /**
   * Parse a JSON object, array, or value into a new instance of the given destination class,
   * optionally using the given parser customizer.
   * <p>
   * If it parses an object, after this method ends, the current token will be the object's ending
   * {@link JsonToken#END_OBJECT}. If it parses an array, after this method ends, the current token
   * will be the array's ending {@link JsonToken#END_ARRAY}.
   * </p>
   *
   * @param dataType Type into which the JSON should be parsed
   * @param close {@code true} if {@link #close()} should be called after parsing
   * @param customizeParser optional parser customizer or {@code null} for none
   * @return new instance of the parsed dataType
   * @since 1.10
   */
  public Object parse(Type dataType, boolean close, CustomizeJsonParser customizeParser)
      throws IOException {
    try {
      startParsing();
      return parseValue(null, dataType, new ArrayList<Type>(), null, customizeParser);
    } finally {
      if (close) {
        close();
      }
    }
  }

  /**
   * Parse a JSON object from the given JSON parser into the given destination object, optionally
   * using the given parser customizer.
   * <p>
   * Before this method is called, the parser must either point to the start or end of a JSON object
   * or to a field name. After this method ends, the current token will be the
   * {@link JsonToken#END_OBJECT} of the current object.
   * </p>
   *
   * @param destination destination object
   * @param customizeParser optional parser customizer or {@code null} for none
   */
  public final void parse(Object destination, CustomizeJsonParser customizeParser)
      throws IOException {
    ArrayList<Type> context = new ArrayList<Type>();
    context.add(destination.getClass());
    parse(context, destination, customizeParser);
  }

  private void parse(
      ArrayList<Type> context, Object destination, CustomizeJsonParser customizeParser)
      throws IOException {
    if (destination instanceof GenericJson) {
      ((GenericJson) destination).setFactory(getFactory());
    }
    JsonToken curToken = startParsingObjectOrArray();
    Class<?> destinationClass = destination.getClass();
    ClassInfo classInfo = ClassInfo.of(destinationClass);
    boolean isGenericData = GenericData.class.isAssignableFrom(destinationClass);
    if (!isGenericData && Map.class.isAssignableFrom(destinationClass)) {
      @SuppressWarnings("unchecked")
      Map<String, Object> destinationMap = (Map<String, Object>) destination;
      parseMap(
          destinationMap, Types.getMapValueParameter(destinationClass), context, customizeParser);
      return;
    }
    while (curToken == JsonToken.FIELD_NAME) {
      String key = getText();
      nextToken();
      // stop at items for feeds
      if (customizeParser != null && customizeParser.stopAt(destination, key)) {
        return;
      }
      // get the field from the type information
      FieldInfo fieldInfo = classInfo.getFieldInfo(key);
      if (fieldInfo != null) {
        // skip final fields
        if (fieldInfo.isFinal() && !fieldInfo.isPrimitive()) {
          throw new IllegalArgumentException("final array/object fields are not supported");
        }
        Field field = fieldInfo.getField();
        int contextSize = context.size();
        context.add(field.getGenericType());
        Object fieldValue =
            parseValue(field, fieldInfo.getGenericType(), context, destination, customizeParser);
        context.remove(contextSize);
        fieldInfo.setValue(destination, fieldValue);
      } else if (isGenericData) {
        // store unknown field in generic JSON
        GenericData object = (GenericData) destination;
        object.set(key, parseValue(null, null, context, destination, customizeParser));
      } else {
        // unrecognized field, skip value
        if (customizeParser != null) {
          customizeParser.handleUnrecognizedKey(destination, key);
        }
        skipChildren();
      }
      curToken = nextToken();
    }
  }

  /**
   * Parse a JSON Array from the given JSON parser (which is closed after parsing completes) into
   * the given destination collection, optionally using the given parser customizer.
   *
   * @param destinationCollectionClass class of destination collection (must have a public default
   *        constructor)
   * @param destinationItemClass class of destination collection item (must have a public default
   *        constructor)
   * @param customizeParser optional parser customizer or {@code null} for none
   */
  public final <T> Collection<T> parseArrayAndClose(Class<?> destinationCollectionClass,
      Class<T> destinationItemClass, CustomizeJsonParser customizeParser) throws IOException {
    try {
      return parseArray(destinationCollectionClass, destinationItemClass, customizeParser);
    } finally {
      close();
    }
  }

  /**
   * Parse a JSON Array from the given JSON parser (which is closed after parsing completes) into
   * the given destination collection, optionally using the given parser customizer.
   *
   * @param destinationCollection destination collection
   * @param destinationItemClass class of destination collection item (must have a public default
   *        constructor)
   * @param customizeParser optional parser customizer or {@code null} for none
   */
  public final <T> void parseArrayAndClose(Collection<? super T> destinationCollection,
      Class<T> destinationItemClass, CustomizeJsonParser customizeParser) throws IOException {
    try {
      parseArray(destinationCollection, destinationItemClass, customizeParser);
    } finally {
      close();
    }
  }

  /**
   * Parse a JSON Array from the given JSON parser into the given destination collection, optionally
   * using the given parser customizer.
   *
   * @param destinationCollectionClass class of destination collection (must have a public default
   *        constructor)
   * @param destinationItemClass class of destination collection item (must have a public default
   *        constructor)
   * @param customizeParser optional parser customizer or {@code null} for none
   */
  public final <T> Collection<T> parseArray(Class<?> destinationCollectionClass,
      Class<T> destinationItemClass, CustomizeJsonParser customizeParser) throws IOException {
    @SuppressWarnings("unchecked")
    Collection<T> destinationCollection =
        (Collection<T>) Data.newCollectionInstance(destinationCollectionClass);
    parseArray(destinationCollection, destinationItemClass, customizeParser);
    return destinationCollection;
  }

  /**
   * Parse a JSON Array from the given JSON parser into the given destination collection, optionally
   * using the given parser customizer.
   *
   * @param destinationCollection destination collection
   * @param destinationItemClass class of destination collection item (must have a public default
   *        constructor)
   * @param customizeParser optional parser customizer or {@code null} for none
   */
  public final <T> void parseArray(Collection<? super T> destinationCollection,
      Class<T> destinationItemClass, CustomizeJsonParser customizeParser) throws IOException {
    parseArray(destinationCollection, destinationItemClass, new ArrayList<Type>(), customizeParser);
  }

  /**
   * Parse a JSON Array from the given JSON parser into the given destination collection, optionally
   * using the given parser customizer.
   *
   * @param destinationCollection destination collection
   * @param destinationItemType type of destination collection item
   * @param customizeParser optional parser customizer or {@code null} for none
   */
  private <T> void parseArray(Collection<T> destinationCollection, Type destinationItemType,
      ArrayList<Type> context, CustomizeJsonParser customizeParser) throws IOException {
    JsonToken curToken = startParsingObjectOrArray();
    while (curToken != JsonToken.END_ARRAY) {
      @SuppressWarnings("unchecked")
      T parsedValue = (T) parseValue(
          null, destinationItemType, context, destinationCollection, customizeParser);
      destinationCollection.add(parsedValue);
      curToken = nextToken();
    }
  }

  private void parseMap(Map<String, Object> destinationMap, Type valueType, ArrayList<Type> context,
      CustomizeJsonParser customizeParser) throws IOException {
    JsonToken curToken = startParsingObjectOrArray();
    while (curToken == JsonToken.FIELD_NAME) {
      String key = getText();
      nextToken();
      // stop at items for feeds
      if (customizeParser != null && customizeParser.stopAt(destinationMap, key)) {
        return;
      }
      Object value = parseValue(null, valueType, context, destinationMap, customizeParser);
      destinationMap.put(key, value);
      curToken = nextToken();
    }
  }

  /**
   * Parse a value.
   *
   * @param field field or {@code null} for none (for example into a map)
   * @param valueType value type or {@code null} if not known (for example into a map)
   * @param context destination context stack (possibly empty)
   * @param destination destination object instance or {@code null} for none (for example empty
   *        context stack)
   * @param customizeParser customize parser or {@code null} for none
   * @return parsed value
   */
  private final Object parseValue(Field field, Type valueType, ArrayList<Type> context,
      Object destination, CustomizeJsonParser customizeParser) throws IOException {
    valueType = Data.resolveWildcardTypeOrTypeVariable(context, valueType);
    // resolve a parameterized type to a class
    Class<?> valueClass = valueType instanceof Class<?> ? (Class<?>) valueType : null;
    if (valueType instanceof ParameterizedType) {
      valueClass = Types.getRawClass((ParameterizedType) valueType);
    }
    // value type is now null, class, parameterized type, or generic array type
    JsonToken token = getCurrentToken();
    switch (token) {
      case START_ARRAY:
      case END_ARRAY:
        boolean isArray = Types.isArray(valueType);
        Preconditions.checkArgument(valueType == null || isArray || valueClass != null
            && Types.isAssignableToOrFrom(valueClass, Collection.class),
            "%s: expected collection or array type but got %s for field %s", getCurrentName(),
            valueType, field);
        Collection<Object> collectionValue = null;
        if (customizeParser != null && field != null) {
          collectionValue = customizeParser.newInstanceForArray(destination, field);
        }
        if (collectionValue == null) {
          collectionValue = Data.newCollectionInstance(valueType);
        }
        Type subType = null;
        if (isArray) {
          subType = Types.getArrayComponentType(valueType);
        } else if (valueClass != null && Iterable.class.isAssignableFrom(valueClass)) {
          subType = Types.getIterableParameter(valueType);
        }
        subType = Data.resolveWildcardTypeOrTypeVariable(context, subType);
        parseArray(collectionValue, subType, context, customizeParser);
        if (isArray) {
          return Types.toArray(collectionValue, Types.getRawArrayComponentType(context, subType));
        }
        return collectionValue;
      case FIELD_NAME:
      case START_OBJECT:
      case END_OBJECT:
        Preconditions.checkArgument(!Types.isArray(valueType),
            "%s: expected object or map type but got %s for field %s", getCurrentName(), valueType,
            field);
        Object newInstance = null;
        if (valueClass != null && customizeParser != null) {
          newInstance = customizeParser.newInstanceForObject(destination, valueClass);
        }
        boolean isMap = valueClass != null && Types.isAssignableToOrFrom(valueClass, Map.class);
        if (newInstance == null) {
          // check if it is a map to avoid ClassCastException to Map
          if (isMap || valueClass == null) {
            newInstance = Data.newMapInstance(valueClass);
          } else {
            newInstance = Types.newInstance(valueClass);
          }
        }
        int contextSize = context.size();
        if (valueType != null) {
          context.add(valueType);
        }
        if (isMap && !GenericData.class.isAssignableFrom(valueClass)) {
          Type subValueType =
              Map.class.isAssignableFrom(valueClass) ? Types.getMapValueParameter(valueType) : null;
          if (subValueType != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> destinationMap = (Map<String, Object>) newInstance;
            parseMap(destinationMap, subValueType, context, customizeParser);
            return newInstance;
          }
        }
        parse(context, newInstance, customizeParser);
        if (valueType != null) {
          context.remove(contextSize);
        }
        return newInstance;
      case VALUE_TRUE:
      case VALUE_FALSE:
        Preconditions.checkArgument(valueType == null || valueClass == boolean.class
            || valueClass != null && valueClass.isAssignableFrom(Boolean.class),
            "%s: expected type Boolean or boolean but got %s for field %s" + field,
            getCurrentName(), valueType, field);
        return token == JsonToken.VALUE_TRUE ? Boolean.TRUE : Boolean.FALSE;
      case VALUE_NUMBER_FLOAT:
      case VALUE_NUMBER_INT:
        Preconditions.checkArgument(field == null || field.getAnnotation(JsonString.class) == null,
            "%s: number type formatted as a JSON number cannot use @JsonString annotation on "
            + "the field %s", getCurrentName(), field);
        if (valueClass == null || valueClass.isAssignableFrom(BigDecimal.class)) {
          return getDecimalValue();
        }
        if (valueClass == BigInteger.class) {
          return getBigIntegerValue();
        }
        if (valueClass == UnsignedInteger.class) {
          return getUnsignedIntegerValue();
        }
        if (valueClass == UnsignedLong.class) {
          return getUnsignedLongValue();
        }
        if (valueClass == Double.class || valueClass == double.class) {
          return getDoubleValue();
        }
        if (valueClass == Long.class || valueClass == long.class) {
          return getLongValue();
        }
        if (valueClass == Float.class || valueClass == float.class) {
          return getFloatValue();
        }
        if (valueClass == Integer.class || valueClass == int.class) {
          return getIntValue();
        }
        if (valueClass == Short.class || valueClass == short.class) {
          return getShortValue();
        }
        if (valueClass == Byte.class || valueClass == byte.class) {
          return getByteValue();
        }
        throw new IllegalArgumentException(getCurrentName() + ": expected numeric type but got "
            + valueType + " for field " + field);
      case VALUE_STRING:
        Preconditions.checkArgument(valueClass == null || !Number.class.isAssignableFrom(valueClass)
            || field != null && field.getAnnotation(JsonString.class) != null,
            "%s: number field formatted as a JSON string must use the @JsonString annotation: %s",
            getCurrentName(), field);
        // TODO(yanivi): "special" values like Double.POSITIVE_INFINITY?
        try {
          return Data.parsePrimitiveValue(valueType, getText());
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException(getCurrentName() + " for field " + field, e);
        }
      case VALUE_NULL:
        Preconditions.checkArgument(valueClass == null || !valueClass.isPrimitive(),
            "%s: primitive number field but found a JSON null: %s", getCurrentName(), field);
        if (valueClass != null
            && 0 != (valueClass.getModifiers() & (Modifier.ABSTRACT | Modifier.INTERFACE))) {
          if (Types.isAssignableToOrFrom(valueClass, Collection.class)) {
            return Data.nullOf(Data.newCollectionInstance(valueType).getClass());
          }
          if (Types.isAssignableToOrFrom(valueClass, Map.class)) {
            return Data.nullOf(Data.newMapInstance(valueClass).getClass());
          }
        }
        return Data.nullOf(Types.getRawArrayComponentType(context, valueType));
      default:
        throw new IllegalArgumentException(getCurrentName() + ": unexpected JSON node type: "
            + token);
    }
  }
}
