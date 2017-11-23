package de.philipphauer.blog.pagination

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.zip.CRC32

internal class PaginationTest{

    //TODO entriesSinceKey should include last element of last page.
    //TODO rework test structure: define all entities up front. make test easier to read and understand.

    @Test
    fun `|1,2,3|4,5,6| different keys`(){
        //Page 1
        val initialEntries = listOf(
                TestPageable(1),
                TestPageable(2),
                TestPageable(3)
        )
        val page = Pagination.createPage(initialEntries, null, 3)
        assertThat(page).isEqualTo(Page(
                entities = listOf(
                        TestPageable(1),
                        TestPageable(2),
                        TestPageable(3)
                ),
                currentToken = ContinuationToken(timestamp = 3, offset = 1, checksum = checksum("3"))
        ))

        //Page 2
        val entriesSinceKey = listOf(
                TestPageable(4),
                TestPageable(5),
                TestPageable(6)
        )
        val page2 = Pagination.createPage(entriesSinceKey, page.currentToken, 3)
        assertThat(page2).isEqualTo(Page(
                entities = listOf(
                        TestPageable(4),
                        TestPageable(5),
                        TestPageable(6)
                ),
                currentToken = ContinuationToken(timestamp = 6, offset = 1, checksum = checksum("6"))
        ))
    }

    @Test
    fun `|1,2,3|3,5,6| key 3 overlaps two pages`(){
        //Page 1
        val initialEntries = listOf(
                TestPageable(1),
                TestPageable(2),
                TestPageable(3)
        )
        val page = Pagination.createPage(initialEntries, null, 3)
        assertThat(page).isEqualTo(Page(
                entities = listOf(
                        TestPageable(1),
                        TestPageable(2),
                        TestPageable(3)
                ),
                currentToken = ContinuationToken(timestamp = 3, offset = 1, checksum = checksum("3"))
        ))

        //Page 2
        val entriesSinceKey = listOf(
                TestPageable("3", 3),
                TestPageable("4", 3),
                TestPageable("5", 5),
                TestPageable("6", 6)
        )
        val page2 = Pagination.createPage(entriesSinceKey, page.currentToken, 3)
        assertThat(page2).isEqualTo(Page(
                entities = listOf(
                        TestPageable("4", 3),
                        TestPageable("5", 5),
                        TestPageable("6", 6)
                ),
                currentToken = ContinuationToken(timestamp = 6, offset = 1, checksum = checksum("6"))
        ))
    }

    @Test
    fun `|1,1,1|1,1,1| all have same key`(){
        //Page 1
        val allEntries = listOf(
                TestPageable("1", 1),
                TestPageable("2", 1),
                TestPageable("3", 1)
        )
        val page = Pagination.createPage(allEntries, null, 3)
        assertThat(page).isEqualTo(Page(
                entities = listOf(
                        TestPageable("1", 1),
                        TestPageable("2", 1),
                        TestPageable("3", 1)
                ),
                currentToken = ContinuationToken(timestamp = 1, offset = 3, checksum = checksum("1", "2", "3"))
        ))

        //Page 2
        val entriesSinceKey = listOf(
                TestPageable("1", 1),
                TestPageable("2", 1),
                TestPageable("3", 1),
                TestPageable("4", 1),
                TestPageable("5", 1),
                TestPageable("6", 1)
        )
        val page2 = Pagination.createPage(entriesSinceKey, page.currentToken, 3)
        assertThat(page2).isEqualTo(Page(
                entities = listOf(
                        TestPageable("4", 1),
                        TestPageable("5", 1),
                        TestPageable("6", 1)
                ),
                currentToken = ContinuationToken(timestamp = 1, offset = 6, checksum = checksum("1", "2", "3", "4", "5", "6"))
        ))
    }

    @Test
    fun `|1,2,3|| although it's the last page it fits right into the page size so we can't tell if this is the last page and have to pass a next token`(){
        //Page 1
        val allEntries = listOf(
                TestPageable(1),
                TestPageable(2),
                TestPageable(3)
        )
        val page = Pagination.createPage(allEntries, null, 3)
        assertThat(page).isEqualTo(Page(
                entities = listOf(
                        TestPageable(1),
                        TestPageable(2),
                        TestPageable(3)
                ),
                currentToken = ContinuationToken(timestamp = 3, offset = 1, checksum = checksum("3"))
        ))

        //Page 2
        val page2 = Pagination.createPage(listOf(), page.currentToken, 3)
        assertThat(page2).isEqualTo(Page(
                entities = listOf(),
                currentToken = null
        ))
    }

    @Test
    fun `|1,2| no next token if there are less elements than page size`(){
        val allEntries = listOf(
                TestPageable(1),
                TestPageable(2)
        )
        val page = Pagination.createPage(allEntries, null, 3)
        assertThat(page).isEqualTo(Page(
                entities = listOf(
                        TestPageable(1),
                        TestPageable(2)
                ),
                currentToken = null
        ))
    }

    @Test
    fun `|1,2,3|4| still skip correctly even if there are less elements than page size`(){
        // Page 1
        val allEntries = listOf(
                TestPageable(1),
                TestPageable(2),
                TestPageable(3)
        )
        val page = Pagination.createPage(allEntries, null, 3)
        assertThat(page).isEqualTo(Page(
                entities = listOf(
                        TestPageable(1),
                        TestPageable(2),
                        TestPageable(3)
                ),
                currentToken = ContinuationToken(timestamp = 3, offset = 1, checksum = checksum("3"))
        ))

        // Page 2
        val entriesSinceKey = listOf(
                TestPageable(3),
                TestPageable(4)
        )
        val page2 = Pagination.createPage(entriesSinceKey, page.currentToken, 3)
        assertThat(page2).isEqualTo(Page(
                entities = listOf(
                        TestPageable(4)
                ),
                currentToken = null
        ))
    }

    @Test
    fun `|1,2,3|4,5|`(){
        // Page 1
        val allEntries = listOf(
                TestPageable(1),
                TestPageable(2),
                TestPageable(3)
        )
        val page = Pagination.createPage(allEntries, null, 3)
        assertThat(page).isEqualTo(Page(
                entities = listOf(
                        TestPageable(1),
                        TestPageable(2),
                        TestPageable(3)
                ),
                currentToken = ContinuationToken(timestamp = 3, offset = 1, checksum = checksum("3"))
        ))

        // Page 2
        val entriesSinceKey = listOf(
                TestPageable(3),
                TestPageable(4),
                TestPageable(5)
        )
        val page2 = Pagination.createPage(entriesSinceKey, page.currentToken, 3)
        assertThat(page2).isEqualTo(Page(
                entities = listOf(
                        TestPageable(4),
                        TestPageable(5)
                ),
                currentToken = null
        ))
    }

    //TODO |1,3,3|4,5,6|
    //TODO |1,3,3|3,5,6|
    //TODO |1,3,3|3,3,6|
    //TODO |1,2,3|3,3,6|
    //TODO |1,2,3|4,4,6|
    //TODO |1,1,1|1,1,2|
    //TODO |1,2,3|4,5|
    //TODO |1,2,3|4,5,6||

    @Nested
    inner class `createPage` {
        @Nested
        inner class `not under page size` {
            @Test
            fun `nothing to skip (offset 1 and next entity has new key)`() {
                val token = ContinuationToken(timestamp = 3, offset = 1, checksum = checksum("3"))
                val pageablesSinceToken = listOf(
                        TestPageable(4),
                        TestPageable(5),
                        TestPageable(6)
                )
                val page = Pagination.createPage(pageablesSinceToken, token, 3)
                assertThat(page).isEqualTo(Page(
                        entities = listOf(
                                TestPageable(4),
                                TestPageable(5),
                                TestPageable(6)
                        ),
                        currentToken = ContinuationToken(timestamp = 6, offset = 1, checksum = checksum("6"))
                ))
            }
            //TODO add more tests
        }
    }

    @Nested
    inner class `createToken` {
        @Test
        fun `only one entity with highest timestamp`() {
            val pageables = listOf(
                    TestPageable(1),
                    TestPageable(2),
                    TestPageable(3),
                    TestPageable(4)
            )
            val token = Pagination.createTokenForPage(pageables, pageables, 4)
            assertThat(token).isEqualTo(ContinuationToken(timestamp = 4, offset = 1, checksum = checksum("4")))
        }
        @Test
        fun `two entities with highest timestamp`() {
            val pageables = listOf(
                    TestPageable(1),
                    TestPageable(2),
                    TestPageable("3", 3),
                    TestPageable("4", 3)
            )
            val token = Pagination.createTokenForPage(pageables, pageables, 4)
            assertThat(token).isEqualTo(ContinuationToken(timestamp = 3, offset = 2, checksum = checksum("3", "4")))
        }
        @Test
        fun `all elements have same timestamp`() {
            val pageables = listOf(
                    TestPageable("1",1),
                    TestPageable("2",1),
                    TestPageable("3",1)
            )
            val token = Pagination.createTokenForPage(pageables, pageables, 3)
            assertThat(token).isEqualTo(ContinuationToken(timestamp = 1, offset = 3, checksum = checksum("1", "2", "3")))
        }
        @Test
        fun `one element list`() {
            val pageables = listOf(
                    TestPageable(1)
            )
            val token = Pagination.createTokenForPage(pageables, pageables, 1)
            assertThat(token).isEqualTo(ContinuationToken(timestamp = 1, offset = 1, checksum = checksum("1")))
        }
        @Test
        fun `empty list`() {
            val token = Pagination.createTokenForPage(listOf(), listOf(), 10)
            assertThat(token).isNull()
        }
        //TODO test varying pagesize!
    }

    @Nested
    inner class `calculateQueryAdvice`{
        @Test
        fun `no token provided`(){
            val advice = Pagination.calculateQueryAdvice(token = null, pageSize = 5)
            assertThat(advice).isEqualTo(QueryAdvice(timestamp = 0, limit = 5))
        }
        @Test
        fun `there was one element with timestamp 20 in the last page`(){
            val token = ContinuationToken(timestamp = 20, offset = 1, checksum = 999)
            val advice = Pagination.calculateQueryAdvice(token, pageSize = 5)
            assertThat(advice).isEqualTo(QueryAdvice(timestamp = 20, limit = 6))
        }
        @Test
        fun `there were 3 elements with timestamp 20 in the last page`(){
            val token = ContinuationToken(timestamp = 20, offset = 3, checksum = 999)
            val advice = Pagination.calculateQueryAdvice(token, pageSize = 5)
            assertThat(advice).isEqualTo(QueryAdvice(timestamp = 20, limit = 8))
        }
    }

    @Nested
    inner class `getEntitiesWithHighestKey`{
        @Test
        fun `all have different keys`(){
            val pageables = listOf(
                    TestPageable(1),
                    TestPageable(2),
                    TestPageable(3)
            )
            val entities = Pagination.getEntitiesWithHighestTimestamp(pageables)
            assertThat(entities).containsExactly(TestPageable(3))
        }
        @Test
        fun `some with the same key`(){
            val pageables = listOf(
                    TestPageable(1),
                    TestPageable(2),
                    TestPageable("4",3),
                    TestPageable("5",3)
                    )
            val entities = Pagination.getEntitiesWithHighestTimestamp(pageables)
            assertThat(entities).containsExactly(TestPageable("4",3), TestPageable("5",3))
        }
        @Test
        fun `all with the same key`(){
            val pageables = listOf(
                    TestPageable("1",1),
                    TestPageable("2",1),
                    TestPageable("3",1)
            )
            val entities = Pagination.getEntitiesWithHighestTimestamp(pageables)
            assertThat(entities).containsExactly(TestPageable("1",1), TestPageable("2",1), TestPageable("3",1))
        }
        @Test
        fun `empty list`(){
            val entities = Pagination.getEntitiesWithHighestTimestamp(listOf())
            assertThat(entities).isEmpty()
        }
        @Test
        fun `only one element`(){
            val pageables = listOf(TestPageable(1))
            val entities = Pagination.getEntitiesWithHighestTimestamp(pageables)
            assertThat(entities).containsExactly(TestPageable("1",1))
        }
    }
}

private fun checksum(vararg ids: String): Long{
    val hash = CRC32()
    hash.update(ids.joinToString("_").toByteArray())
    return hash.value
}

data class TestPageable(
        private val id: String,
        private val timestamp: Long
): Pageable {
    constructor(timestamp: Long): this(timestamp.toString(), timestamp)
    override fun getID() = id
    override fun getTimestamp() = timestamp
}