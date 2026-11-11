package com.example.mdeps_application.pto.data

import com.example.mdeps_application.pto.data.remote.dto.BoardResourceDto
import com.example.mdeps_application.pto.domain.Project

/**
 * Maps local [Project.id] to backend Kanban [boardId] values.
 */
class ProjectBoardRegistry {

    private val projectToBoard = mutableMapOf<Long, Int>()

    fun boardIdFor(projectId: Long): Int? = projectToBoard[projectId]

    fun register(projectId: Long, boardId: Int) {
        projectToBoard[projectId] = boardId
    }

  fun hydrate(boards: List<BoardResourceDto>, projects: List<Project>) {
        boards.forEach { board ->
            val boardId = board.id ?: return@forEach
            val byProjectId = board.projectId?.toLong()
            if (byProjectId != null && projects.any { it.id == byProjectId }) {
                register(byProjectId, boardId)
            }
            val projectName = board.project?.trim().orEmpty()
            if (projectName.isNotEmpty()) {
                projects.firstOrNull { it.name.equals(projectName, ignoreCase = true) }
                    ?.let { register(it.id, boardId) }
            }
            val boardName = board.name?.trim().orEmpty()
            if (boardName.isNotEmpty()) {
                projects.firstOrNull { it.name.equals(boardName, ignoreCase = true) }
                    ?.let { register(it.id, boardId) }
            }
        }
        projects.forEach { project ->
            if (projectToBoard.containsKey(project.id)) return@forEach
            boards.firstOrNull { it.id == project.id.toInt() }?.id?.let { register(project.id, it) }
        }
    }

    fun resolveBoardId(project: Project, boards: List<BoardResourceDto>): Int {
        boardIdFor(project.id)?.let { return it }
        hydrate(boards, listOf(project))
        return boardIdFor(project.id) ?: project.id.toInt()
    }
}
