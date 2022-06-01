package com.example.tagtabazaradmin

data class Category(
    val id: String?,
    val Name: String?,
    val Order: Int?
){
    constructor() : this(
        null,null,null
    )
}
