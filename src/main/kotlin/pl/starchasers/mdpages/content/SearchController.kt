package pl.starchasers.mdpages.content

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SearchController(
    private val searchService: SearchService
) {
    @GetMapping("/api/search")
    fun search(@RequestParam query: String?, @RequestParam size: Int?, @RequestParam page: Int?) =
        searchService.execSearch(query, size ?: 10, page ?: 0)


}