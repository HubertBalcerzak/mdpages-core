package pl.starchasers.mdpages.content

import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.SimpleQueryStringFlag
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import pl.starchasers.mdpages.content.data.Page
import pl.starchasers.mdpages.content.data.dto.PageSearchResultDTO
import pl.starchasers.mdpages.content.data.dto.SearchResponseDTO
import pl.starchasers.mdpages.content.repository.PageRepository
import java.time.LocalDate

const val DOCUMENT_INDEX_NAME = "mdpages_documents"

interface SearchService {

    fun execSearch(query: String?, pageSize: Int, page: Int): SearchResponseDTO

    fun indexPage(page: Page)

    fun deleteIndexedPage(page: Page)

    fun recreateIndex()
}

@Service
class SearchServiceImpl(
    private val pageRepository: PageRepository,
    @Qualifier("client") private val restClient: RestHighLevelClient

) : SearchService {

    val regexTitleSearch = Regex("title:(.+)")
    val regexDateSearch = Regex("date:(|>|<)([0-9]{1,2})[./]([0-9]{1,2})[./]([0-9]{4})")
    val regexDateAndContentSearch = Regex("date:(|>|<)([0-9]{1,2})[./]([0-9]{1,2})[./]([0-9]{4}) (.+)")

    override fun indexPage(page: Page) {
        restClient.index(
            IndexRequest(DOCUMENT_INDEX_NAME)
                .id(page.id.toString())
                .source(
                    mapOf(
                        Pair("content", page.content),
                        Pair("title", page.name),
                        Pair("date", page.lastEdited.toLocalDate())
                    )
                ),
            RequestOptions.DEFAULT
        )
    }

    override fun deleteIndexedPage(page: Page) {
        restClient.delete(DeleteRequest(DOCUMENT_INDEX_NAME, page.id.toString()), RequestOptions.DEFAULT)
    }

    override fun execSearch(query: String?, pageSize: Int, page: Int): SearchResponseDTO {
        val pages: List<Page>
        val totalPages: Int
        if (query == null || query.isBlank()) {
            pages = pageRepository.findAll(PageRequest.of(page, pageSize)).content
            totalPages = pageRepository.count().toInt();
        } else {

            val response = restClient.search(searchSourceBuilderQuery(query, pageSize, page), RequestOptions.DEFAULT)

            if (response.status() == RestStatus.OK) {
                pages = response.hits.mapNotNull { pageRepository.findFirstById(it.id.toLong()) }.toList()
                totalPages = response.hits.totalHits?.value?.toInt()
                    ?: throw RuntimeException("Error when executing search query");
            } else {
                throw RuntimeException("Error when executing search query");
            }
        }

        return SearchResponseDTO(pages.map {
            PageSearchResultDTO(
                it.id,
                it.name,
                it.lastEdited
            )
        }, totalPages)
    }

    private fun searchSourceBuilderQuery(query: String, pageSize: Int, page: Int): SearchRequest {
        val searchRequest = SearchRequest(DOCUMENT_INDEX_NAME)
        val queryBuilder: QueryBuilder = when {
            regexTitleSearch.matches(query) -> {
                val (title) = regexTitleSearch.find(query)!!.destructured
                QueryBuilders
                    .simpleQueryStringQuery(title)
                    .field("title")
            }
            regexDateSearch.matches(query) -> {
                val (symbol, day, month, year) = regexDateSearch.find(query)!!.destructured
                getDateRangeQuery(symbol, day, month, year)
            }
            regexDateAndContentSearch.matches(query) -> {
                val (symbol, day, month, year, contentQuery) = regexDateAndContentSearch
                    .find(query)!!.destructured
                QueryBuilders.boolQuery()
                    .must(getDateRangeQuery(symbol, day, month, year))
                    .must(
                        QueryBuilders.simpleQueryStringQuery(contentQuery)
                            .field("content")
                            .field("title")
                            .boost(5f)
                            .flags(
                                SimpleQueryStringFlag.AND,
                                SimpleQueryStringFlag.NOT,
                                SimpleQueryStringFlag.OR,
                                SimpleQueryStringFlag.PHRASE,
                                SimpleQueryStringFlag.PRECEDENCE
                            )
                    )
            }
            else -> {
                QueryBuilders
                    .simpleQueryStringQuery(query)
                    .field("content")
                    .field("title").boost(5f)
                    .flags(
                        SimpleQueryStringFlag.AND,
                        SimpleQueryStringFlag.NOT,
                        SimpleQueryStringFlag.OR,
                        SimpleQueryStringFlag.PHRASE,
                        SimpleQueryStringFlag.PRECEDENCE
                    )
            }
        }
        return searchRequest.source(
            SearchSourceBuilder()
                .query(queryBuilder)
                .fetchSource(false)
                .size(pageSize)
                .from(pageSize * page)
        )
    }

    private fun getDateRangeQuery(
        symbol: String,
        day: String,
        month: String,
        year: String
    ): QueryBuilder {
        val date = LocalDate.of(year.toInt(), month.toInt(), day.toInt())
        return when (symbol) {
            ">" -> QueryBuilders.rangeQuery("date").gt(date)
            "<" -> QueryBuilders.rangeQuery("date").lt(date)
            else -> QueryBuilders.matchQuery("date", date)
        }
    }

    override fun recreateIndex() {
        TODO("Not yet implemented")
    }


}