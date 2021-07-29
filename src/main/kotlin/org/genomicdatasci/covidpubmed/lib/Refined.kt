package org.genomicdatasci.covidpubmed.lib

/**
 * Created by fcriscuo on 7/28/21.
 */
interface Refined<in T> {
    abstract fun isValid(value: T) : Boolean
}