package com.example.supertodolist.util

// this is an extension property to make any object return by it self
// meaning when call this property will have a return type of the same object
// it will be used with "when()" in kotlin to get compile time safety
val <T> T.exhaustive: T
        get() = this