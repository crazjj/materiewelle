package materiewelle.state

import kotlinx.browser.window
import materiewelle.core.course.ReadingPosition

/** This key contains only chapter navigation; personal answers stay in SessionStorage. */
object ReadingPositionStore {
    const val KEY = "materiewelle.reading.position.v1"
    fun load(): ReadingPosition? = runCatching {
        window.localStorage.getItem(KEY)?.let(ReadingPosition::parse)
    }.getOrNull()
    fun save(position: ReadingPosition) {
        runCatching { window.localStorage.setItem(KEY, position.json()) }
    }
}
