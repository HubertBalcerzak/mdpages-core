package pl.starchasers.mdpages.content.data.dto

import java.time.LocalDateTime

class PageSearchResultDTO(
    val id: Long,
    val name: String,
    val dateModified: LocalDateTime
)