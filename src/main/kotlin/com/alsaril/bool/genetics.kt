package com.alsaril.bool

import java.util.*

interface Genetics<T> {
    fun mutate(t: T): T
    fun cross(a: T, b: T): T
    fun reduce(t: T): T
    fun random(): T
}
