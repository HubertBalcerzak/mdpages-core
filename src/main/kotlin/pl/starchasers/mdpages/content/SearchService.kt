package pl.starchasers.mdpages.content

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import pl.starchasers.mdpages.content.data.Page
import pl.starchasers.mdpages.content.repository.PageRepository

interface SearchService {

    fun execSearch(query: String?): List<Page>
}

@Service
class SearchServiceImpl(
    private val pageRepository: PageRepository
) : SearchService {


    override fun execSearch(query: String?): List<Page> {
        if (query == null || query.isBlank()) {
            return pageRepository.findAll(PageRequest.of(0, 10)).content
        } else {
            TODO()
        }
    }
}