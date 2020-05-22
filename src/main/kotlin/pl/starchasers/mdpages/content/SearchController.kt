package pl.starchasers.mdpages.content

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pl.starchasers.mdpages.content.data.dto.PageSearchResultDTO
import pl.starchasers.mdpages.content.data.dto.SearchResponseDTO

@RestController
class SearchController(
    private val searchService: SearchService
) {
    @GetMapping("/api/search")
    fun search(@RequestParam query: String?) = SearchResponseDTO(
        searchService.execSearch(query).map {
            PageSearchResultDTO(
                it.id,
                it.name,
                it.lastEdited
            )
        }
    )

}