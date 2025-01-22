/*
 *    Copyright 2025 magicmq
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dev.magicmq.pyspigot.util;

import org.python.antlr.AST;
import org.python.core.Py;
import org.python.core.PyBuiltinCallable;
import org.python.core.PyFloat;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A utility class for handling mixed positionitional and keyword arguments when calling Java methods from Python code.
 * <p>
 * Adapted from {@link org.python.core.ArgParser}
 */
public class ArgParser {

    private static final Object REQUIRED = new Object();

    private final String methodName;
    private final PyObject[] args;
    private final String[] keywords;

    private String[] params = null;

    private ArgParser(String methodName, PyObject[] args, String[] keywords) {
        this.methodName = methodName;
        this.args = args;
        this.keywords = Objects.requireNonNullElseGet(keywords, () -> new String[0]);
    }

    /**
     * Create an ArgParser for a method that takes one argument.
     * @param methodName The name of the method whose arguments are being parsed. Used in error messages
     * @param args The arguments supplied with the call
     * @param keywords The keywords supplied with the call
     * @param p0 The keyword for the argument
     */
    public ArgParser(String methodName, PyObject[] args, String[] keywords, String p0) {
        this(methodName, args, keywords);
        this.params = new String[] { p0 };
        check();
    }

    /**
     * Create an ArgParser for a method that takes two arguments.
     * @param methodName The name of the method whose arguments are being parsed. Used in error messages
     * @param args The arguments supplied with the call
     * @param keywords The keywords supplied with the call
     * @param p0 The first expected keyword in the method definition
     * @param p1 The second expected keyword in the method definition
     */
    public ArgParser(String methodName, PyObject[] args, String[] keywords, String p0, String p1) {
        this(methodName, args, keywords);
        this.params = new String[] { p0, p1 };
        check();
    }

    /**
     * Create an ArgParser for a method that takes three arguments.
     * @param methodName The name of the method whose arguments are being parsed. Used in error messages
     * @param args The arguments supplied with the call
     * @param keywords The keywords supplied with the call
     * @param p0 The first expected keyword in the method definition
     * @param p1 The second expected keyword in the method definition
     * @param p2 The third expected keyword in the method definition
     */
    public ArgParser(String methodName, PyObject[] args, String[] keywords, String p0, String p1, String p2) {
        this(methodName, args, keywords);
        this.params = new String[] { p0, p1, p2 };
        check();
    }

    /**
     * Create an ArgParser for a method that takes one or more arguments.
     * @param methodName The name of the method whose arguments are being parsed. Used in error messages
     * @param args The arguments supplied with the call
     * @param keywords The keywords supplied with the call
     * @param params An array of expected keywords in the method definition
     */
    public ArgParser(String methodName, PyObject[] args, String[] keywords, String[] params) {
        this(methodName, args, keywords);
        this.params = params;
        check();
    }

    /**
     * Create an ArgParser for a method that takes one or more arguments and requires a minimum number of arguments.
     * <p>
     * Uses {@link org.python.core.PyBuiltinCallable.DefaultInfo} to validate minArgs
     * @param methodName The name of the method whose arguments are being parsed. Used in error messages
     * @param args The arguments supplied with the call
     * @param keywords The keywords supplied with the call
     * @param params An array of expected keywords in the method definition
     * @param minArgs The minimum number of arguments the method should accept
     */
    public ArgParser(String methodName, PyObject[] args, String[] keywords, String[] params, int minArgs) {
        this(methodName, args, keywords);
        this.params = params;
        check();
        
        if (!PyBuiltinCallable.DefaultInfo.check(args.length, minArgs, this.params.length)) {
            throw PyBuiltinCallable.DefaultInfo.unexpectedCall(args.length, false, methodName, minArgs, this.params.length);
        }
    }

    /**
     * Create an ArgParser for a method that takes one or more arguments and requires a minimum number of arguments, and optionally takes zero arguments.
     * <p>
     * Uses {@link org.python.antlr.AST} to validate minArgs and takesZeroArgs
     * @param methodName The name of the method whose arguments are being parsed. Used in error messages
     * @param args The arguments supplied with the call
     * @param keywords The keywords supplied with the call
     * @param params An array of expected keywords in the method definition
     * @param minArgs The minimum number of arguments the method should accept
     * @param takesZeroArgs True if the 
     */
    public ArgParser(String methodName, PyObject[] args, String[] keywords, String[] params, int minArgs, boolean takesZeroArgs) {
        this(methodName, args, keywords);
        this.params = params;
        check();
        
        if (!AST.check(args.length - keywords.length, minArgs, takesZeroArgs)) {
            throw AST.unexpectedCall(minArgs, methodName);
        }
    }

    /**
     * Return a required argument as a Java String.
     * @param position The position of the argument
     * @return The argument as a String
     * @throws org.python.core.PyException Raises a TypeError (via a PyException) if the required argument was not passed
     * @throws org.python.core.PyException Raises a TypeError (via a PyException) if the passed value is not coercible to a Java String
     */
    public String getString(int position) {
        return (String) getArg(position, String.class, "String");
    }

    /**
     * Return an optional argument as a Java String.
     * @param position The position of the argument
     * @return The argument as a String, or the default value if the argument was not passed
     * @param def The default value to return if the argument was not passed
     * @throws org.python.core.PyException Raises a TypeError (via a PyException) if the passed value is not coercible to a Java String
     */
    public String getString(int position, String def) {
        return (String) getArg(position, String.class, "String", def);
    }

    /**
     * Return a required argument as a Java int.
     *
     * @param position The position of the argument
     * @return The argument as an int
     * @throws org.python.core.PyException Raises a TypeError (via a PyException) if the required argument was not passed
     * @throws org.python.core.PyException Raises a TypeError (via a PyException) if the passed value is not coercible to a Java int
     */
    public int getInt(int position) {
        return asInt(position, getRequiredArg(position));
    }

    /**
     * Return an optional argument as a Java int.
     * @param position The position of the argument
     * @return The argument as an int, or the default value if the argument was not passed
     * @throws org.python.core.PyException Raises a TypeError (via a PyException) if the passed value is not coercible to a Java int
     */
    public int getInt(int position, int def) {
        PyObject value = getOptionalArg(position);
        if (value == null) {
            return def;
        }
        return asInt(position, value);
    }

    /**
     * Return a required argument as a Java list.
     * @param position The position of the argument
     * @param clazz The Java type to which the list items should be converted
     * @return The argument as a Java List containing items of the specified type
     * @param <T> The type of items within the list
     * @throws org.python.core.PyException Raises a TypeError (via a PyException) if the required argument was not passed
     * @throws org.python.core.PyException Raises a TypeError (via a PyException) if the passed value is not coercible to a Java List or if any of the items within the list are not coercible to the specified type
     */
    public <T> List<T> getList(int position, Class<T> clazz) {
        return getListArgRequired(position, clazz);
    }

    /**
     * Return a required argument as a Java list.
     * @param position The position of the argument
     * @param clazz The Java type to which the list items should be converted
     * @return The argument as a Java List containing items of the specified type
     * @param <T> The type of items within the list
     * @throws org.python.core.PyException Raises a TypeError (via a PyException) if the required argument was not passed
     * @throws org.python.core.PyException Raises a TypeError (via a PyException) if the passed value is not coercible to a Java List or if any of the items within the list are not coercible to the specified type
     */
    public <T> List<T> getList(int position, Class<T> clazz, List<T> def) {
        return getListArgOptional(position, clazz, def);
    }

    /**
     * Return a required argument as a Java Object. Will attempt to coerce the PyObject into the specified type.
     * @param position The position of the argument
     * @param clazz The class corresponding to the type the object should be coerced to
     * @return The argument as the specified type
     * @param <T> The Java type to coerce the PyObject to
     * @throws org.python.core.PyException Raises a TypeError (via a PyException) if the required argument was not passed
     * @throws org.python.core.PyException Raises a TypeError (via a PyException) if the passed value is not coercible to the specified type
     */
    @SuppressWarnings("unchecked")
    public <T> T getJavaObject(int position, Class<T> clazz) {
        return (T) getArg(position, clazz, clazz.getName());
    }

    /**
     * Return an optional argument as a Java Object. Will attempt to coerce the PyObject into the specified type.
     * @param position The position of the argument
     * @param clazz The class corresponding to the type the object should be coerced to
     * @return The argument as the specified type, or the default value if the argument was not passed
     * @param <T> The Java type to coerce the PyObject to
     * @throws org.python.core.PyException Raises a TypeError (via a PyException) if the passed value is not coercible to the specified type
     */
    @SuppressWarnings("unchecked")
    public <T> T getJavaObject(int position, Class<T> clazz, T def) {
        return (T) getArg(position, clazz, clazz.getName(), def);
    }

    /**
     * Return a required argument as a PyObject.
     * @param position The position of the argument
     * @return The argument as a PyObject
     */
    public PyObject getPyObject(int position) {
        return getRequiredArg(position);
    }

    /**
     * Return an optional argument as a PyObject.
     * @param position The position of the argument
     * @return The argument as a PyObject, or the default value if the argument was not passed
     */
    public PyObject getPyObject(int position, PyObject def) {
        PyObject value = getOptionalArg(position);
        if (value == null) {
            value = def;
        }
        return value;
    }

    /**
     * Return a required argument as a PyObject, ensuring the object is of the specified type.
     *
     * @param position the position of the argument. First argument is numbered 0
     * @param type the desired PyType of the argument
     * @return The argument as a PyObject of the specified type
     * @throws org.python.core.PyException Raises a TypeError (via a PyException) if the passed value is not coercible to the specified type
     */
    public PyObject getPyObjectByType(int position, PyType type) {
        PyObject arg = getRequiredArg(position); // != null
        return checkForType(arg, position, type);
    }

    /**
     * Return an optional argument as a PyObject, ensuring the object is of the specified type.
     * @param position the position of the argument
     * @param type the desired PyType of the argument
     * @param def to return if the argument at position was not given (null allowed)
     * @return The argument as a PyObject of the specified type, or the default value if the argument was not passed
     * @throws org.python.core.PyException Raises a TypeError (via a PyException) if the passed value is not coercible to the specified type
     */
    public PyObject getPyObjectByType(int position, PyType type, PyObject def) {
        PyObject arg = getOptionalArg(position);
        return checkForType((arg != null ? arg : def), position, type);
    }

    /**
     * Return a required argument as an index.
     * @param position The position of the argument
     * @return The index of the argument
     * @throws org.python.core.PyException Raises a TypeError (via a PyException) if the required argument was not passed
     */
    public int getIndex(int position) {
        PyObject value = getRequiredArg(position);
        return value.asIndex();
    }

    /**
     * Return an optional argument as an index.
     * @param position The position of the argument.
     * @return The index of the argument, or the default value if the argument was not passed
     */
    public int getIndex(int position, int def) {
        PyObject value = getOptionalArg(position);
        if (value == null) {
            return def;
        }
        return value.asIndex();
    }

    /**
     * Return the remaining arguments as a tuple after the specified position.
     * @param position The position of the argument to start at
     * @return A tuple containing the remaining arguments after the specified position
     */
    public PyObject getList(int position) {
        int keywords_start = this.args.length - this.keywords.length;
        if (position < keywords_start) {
            PyObject[] ret = new PyObject[keywords_start - position];
            System.arraycopy(this.args, position, ret, 0, keywords_start - position);
            return new PyTuple(ret);
        }
        return Py.EmptyTuple;
    }

    /**
     * Ensure no keyword arguments were passed.
     * <p>
     * Raises a TypeError (via a PyException) if any keyword arguments were passed.
     */
    public void noKeywords() {
        if (keywords.length > 0) {
            throw Py.TypeError(String.format(
                    "%s() does not take keyword arguments",
                    methodName));
        }
    }

    private void check() {
        Set<Integer> usedKeywords = new HashSet<>();
        int nargs = args.length - keywords.length;
        outer:
        for (String keyword : keywords) {
            for (int j = 0; j < params.length; j++) {
                if (keyword.equals(params[j])) {
                    if (j < nargs) {
                        throw Py.TypeError(String.format(
                                "%s(): keyword parameter '%s' was given by position and by name",
                                methodName,
                                params[j]));
                    }
                    if (usedKeywords.contains(j)) {
                        throw Py.TypeError(String.format(
                                "%s(): got multiple values for keyword argument '%s'",
                                methodName,
                                params[j]));
                    }
                    usedKeywords.add(j);
                    continue outer;
                }
            }
            throw Py.TypeError(String.format(
                    "%s(): '%s' is an invalid keyword argument for this function",
                    methodName,
                    keyword));
        }
    }

    private PyObject getRequiredArg(int position) {
        PyObject toReturn = getOptionalArg(position);
        if (toReturn == null) {
            throw Py.TypeError(String.format(
                    "%s(): The %s argument is required",
                    methodName,
                    getParam(position)));
        }
        return toReturn;
    }

    private PyObject getOptionalArg(int position) {
        int keywordsStart = this.args.length - this.keywords.length;
        if (position < keywordsStart) {
            return this.args[position];
        }
        for (int i = 0; i < this.keywords.length; i++) {
            if (this.keywords[i].equals(this.params[position])) {
                return this.args[keywordsStart + i];
            }
        }
        return null;
    }

    private Object getArg(int position, Class<?> clazz, String className) {
        return getArg(position, clazz, className, REQUIRED);
    }

    private Object getArg(int position, Class<?> clazz, String className, Object def) {
        PyObject value;
        if (def == REQUIRED) {
            value = getRequiredArg(position);
        } else {
            value = getOptionalArg(position);
            if (value == null) {
                return def;
            }
        }

        Object ret = value.__tojava__(clazz);
        if (ret == Py.NoConversion) {
            throw Py.TypeError(String.format(
                    "%s(): expected type %s for argument %s but got %s",
                    methodName,
                    className,
                    getParam(position),
                    value.getType().fastGetName()));
        }
        return ret;
    }

    private <T> List<T> getListArgRequired(int position, Class<T> type) {
        PyObject value = getRequiredArg(position);

        if (value instanceof PyList pyList) {
            return convertListItems(pyList, type, position);
        } else {
            throw Py.TypeError(String.format(
                    "%s(): expected type list for argument %s but got %s",
                    methodName,
                    getParam(position),
                    value.getType().fastGetName()));
        }
    }

    private <T> List<T> getListArgOptional(int position, Class<T> type, List<T> def) {
        PyObject value = getOptionalArg(position);
        if (value == null)
            return def;

        if (value instanceof PyList pyList) {
            return convertListItems(pyList, type, position);
        } else {
            throw Py.TypeError(String.format(
                    "%s(): expected type list for argument %s but got %s",
                    methodName,
                    getParam(position),
                    value.getType().fastGetName()));
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> convertListItems(PyList list, Class<T> type, int position) {
        List<T> result = new ArrayList<>();
        for (PyObject item : list.asIterable()) {
            Object converted = item.__tojava__(type);
            if (converted == Py.NoConversion) {
                throw Py.TypeError(String.format(
                        "%s(): type %s can't be coerced to %s in argument %s",
                        methodName,
                        item.getType().fastGetName(),
                        type.getName(),
                        getParam(position)));
            }
            result.add((T) converted);
        }
        return result;
    }

    private int asInt(int position, PyObject value) {
        if (value instanceof PyFloat) {
            Py.warning(Py.DeprecationWarning, String.format(
                    "%s(): integer expected for argument %s but got float",
                    methodName,
                    getParam(position)));
            value = value.__int__();
        }
        return value.asInt();
    }

    private PyObject checkForType(PyObject arg, int position, PyType type) {
        if (arg == null || Py.isInstance(arg, type)) return arg;
        throw Py.TypeError(String.format(
                "%s(): argument %s must be of type %s, not %s",
                methodName,
                getParam(position),
                type.fastGetName(),
                arg.getType().fastGetName()));
    }

    private String getParam(int position) {
        return "'" + this.params[position] + "'";
    }
}
