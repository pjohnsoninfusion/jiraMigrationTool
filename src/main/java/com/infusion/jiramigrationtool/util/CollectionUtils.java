package com.infusion.jiramigrationtool.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

public class CollectionUtils {

    private final static Logger logger = LoggerFactory.getLogger(CollectionUtils.class.getName());

    /**
     * Converts a String array to an ImmutableSet
     * 
     * @param inputArray
     * @return
     */
    public static ImmutableSet<String> arrayToImmutableSet(final String[] inputArray) {
        final Set<String> temp = new HashSet<String>();
        try {
            temp.addAll(Arrays.asList(inputArray));
            for (String element : temp) {
                if (element.equals("") || element.equals(" ")) {
                    element = null;
                }
            }
        } catch (final Exception e) {
            logger.warn("{}", e.getMessage(), e);
        }
        return ImmutableSet.copyOf(temp);
    }

    /**
     * Performs a split and trim to convert an input string to an array. Does
     * not include empty values.
     * 
     * @param delimiter
     * @param inputString
     * @return
     */
    public static String[] stringToArray(final String delimiter, final String inputString) {
        try {
            if ((inputString == null) || inputString.isEmpty()) {
                return new String[] {};
            }
            final String[] inputStringSplit = inputString.split(delimiter);
            final Set<String> inputStringsTrimmed = new HashSet<String>();
            for (final String curr : inputStringSplit) {
                if (!curr.trim().isEmpty()) {
                    inputStringsTrimmed.add(curr.trim());
                }
            }
            final String[] myArray = inputStringsTrimmed.toArray(new String[inputStringsTrimmed.size()]);
            return myArray;
        } catch (final Exception e) {
            logger.warn("{}", e.getMessage(), e);
            return new String[] {};
        }
    }

    /**
     * Convenience method that combines stringToArray and arrayToImmutableSet
     * 
     * @param delimiter
     * @param inputString
     * @see stringToArray
     * @see arrayToImmutableSet
     * @return
     */
    public static ImmutableSet<String> stringToImmutableSet(final String delimiter, final String inputString) {
        try {
            return arrayToImmutableSet(stringToArray(delimiter, inputString));
        } catch (final Exception e) {
            logger.warn("{}", e.getMessage(), e);
        }
        return ImmutableSet.copyOf(new HashSet<String>());
    }

}
