package pl.starchasers.mdpages.content

import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import pl.starchasers.mdpages.content.data.Page
import pl.starchasers.mdpages.content.repository.PageRepository
import javax.swing.plaf.multi.MultiDesktopPaneUI

const val DOCUMENT_INDEX_NAME = "mdpages_documents"

interface SearchService {

    fun execSearch(query: String?): List<Page>

    fun indexPage(page: Page)

    fun recreateIndex()
}

@Service
class SearchServiceImpl(
    private val pageRepository: PageRepository,
    @Qualifier("client") private val restClient: RestHighLevelClient

) : SearchService {


    override fun execSearch(query: String?): List<Page> {
        if (query == null || query.isBlank()) {
            return pageRepository.findAll(PageRequest.of(0, 10)).content
        } else {
            TODO()
        }
    }

    override fun indexPage(page: Page) {
        restClient.index(
            IndexRequest(DOCUMENT_INDEX_NAME)
                .id(page.id.toString())
                .source(
                    mapOf(
                        Pair("content", page.content),
                        Pair("title", page.name)
                    )
                ),
            RequestOptions.DEFAULT
        )
    }

    override fun recreateIndex() {
        TODO("Not yet implemented")
    }


}