package pt.unl.fct.di.pdf_html_rss_core.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Repository
import pt.unl.fct.di.pdf_html_rss_core.dto.PendingRedactionTask
import java.util.Optional

@Repository
class PendingRedactionTasksRepository : CrudRepository<PendingRedactionTask, String> {

    val pendingRedactionTasks = mutableMapOf<String, PendingRedactionTask>()

    override fun <S : PendingRedactionTask> save(task: S): S {
        pendingRedactionTasks[task.taskId] = task;
        return task;
    }

    override fun <S : PendingRedactionTask> saveAll(entities: MutableIterable<S>): MutableIterable<S> {
        entities.forEach {
            this.save(it)
        }
        return entities
    }

    override fun findById(id: String): Optional<PendingRedactionTask> {
        return Optional.ofNullable(pendingRedactionTasks[id])
    }

    override fun existsById(id: String): Boolean {
        return pendingRedactionTasks.containsKey(id)
    }

    override fun findAll(): MutableIterable<PendingRedactionTask> {
        TODO("Not yet implemented")
    }

    override fun count(): Long {
        return pendingRedactionTasks.size.toLong();
    }

    override fun deleteAll() {
        pendingRedactionTasks.clear();
    }

    override fun deleteAll(tasks: MutableIterable<PendingRedactionTask>) {
        tasks.forEach {
            this.delete(it)
        }
    }

    override fun deleteAllById(ids: MutableIterable<String>) {
        ids.forEach {
            this.deleteById(it)
        }
    }

    override fun delete(task: PendingRedactionTask) {
        pendingRedactionTasks.remove(task.taskId)
    }

    override fun deleteById(taskId: String) {
        pendingRedactionTasks.remove(taskId)
    }

    override fun findAllById(ids: MutableIterable<String>): MutableIterable<PendingRedactionTask> {
        TODO("Not yet implemented")
    }

//    @Scheduled
    fun pruneTasks() {
        TODO("Not yet implemented")
    }

}