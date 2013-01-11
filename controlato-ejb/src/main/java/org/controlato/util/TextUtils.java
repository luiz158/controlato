/* Controlato is a web-based service conceived and designed to assist families
 * on the control of their daily lives.
 * Copyright (C) 2012 Controlato.org
 *
 * Controlato is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Controlato is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with Controlato. Look for the file LICENSE.txt at the root level.
 * If you do not, see http://www.gnu.org/licenses/.
 * */
package org.controlato.util;

/**
 * This class groups a set of methods to deal with special text requirements
 * that are not already covered by the Java API.
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
public class TextUtils {

    /** Receives a sentence and converts the first letter of each word to a
     * capital letter and the rest of each word to lowercase. */
    public static String capitalizeFirstCharWords(String sentence) {
        final StringBuilder result = new StringBuilder(sentence.length());
        String[] words = sentence.split("\\s");
        for(int i = 0,length = words.length; i < length; ++i) {
            if(i > 0) {
                result.append(" ");
            }
            result.append(Character.toUpperCase(words[i].charAt(0)))
                  .append(words[i].substring(1).toLowerCase());
        }
        return result.toString();
    }
}