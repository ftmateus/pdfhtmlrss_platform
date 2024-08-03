package pt.unl.fct.di.pdf_html_rss_core.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import pt.unl.fct.di.pdf_html_rss_core.dto.RedactionProcess
import java.util.Optional

@Repository
class RedactionProcessRepository : CrudRepository<RedactionProcess, String> {

    val pendingRedactionProcesses = mutableMapOf<String, RedactionProcess>()

    override fun <S : RedactionProcess> save(task: S): S {
        pendingRedactionProcesses[task.taskId] = task;
        return task;
    }

    override fun <S : RedactionProcess> saveAll(entities: MutableIterable<S>): MutableIterable<S> {
        entities.forEach {
            this.save(it)
        }
        return entities
    }

    override fun findById(id: String): Optional<RedactionProcess> {
        return Optional.ofNullable(pendingRedactionProcesses[id])
    }

    override fun existsById(id: String): Boolean {
        return pendingRedactionProcesses.containsKey(id)
    }

    override fun findAll(): MutableIterable<RedactionProcess> {
        TODO("Not yet implemented")
    }

    override fun count(): Long {
        return pendingRedactionProcesses.size.toLong();
    }

    override fun deleteAll() {
        pendingRedactionProcesses.clear();
    }

    override fun deleteAll(tasks: MutableIterable<RedactionProcess>) {
        tasks.forEach {
            this.delete(it)
        }
    }

    override fun deleteAllById(ids: MutableIterable<String>) {
        ids.forEach {
            this.deleteById(it)
        }
    }

    override fun delete(process: RedactionProcess) {
        pendingRedactionProcesses.remove(process.taskId)
    }

    override fun deleteById(processId: String) {
        pendingRedactionProcesses.remove(processId)
    }

    override fun findAllById(ids: MutableIterable<String>): MutableIterable<RedactionProcess> {
        TODO("Not yet implemented")
    }

//    @Scheduled
    fun pruneTasks() {
        TODO("Not yet implemented")
    }

}