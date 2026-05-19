/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.ac.hcw.procrastinot.data

import at.ac.hcw.procrastinot.data.source.local.LocalTask
import at.ac.hcw.procrastinot.data.source.network.NetworkTask
import at.ac.hcw.procrastinot.data.source.network.TaskStatus

/**
 * Data model mapping extension functions. There are three model types:
 *
 * - Task: External model exposed to other layers in the architecture.
 * Obtained using `toExternal`.
 *
 * - NetworkTask: Internal model used to represent a task from the network. Obtained using
 * `toNetwork`.
 *
 * - LocalTask: Internal model used to represent a task stored locally in a database. Obtained
 * using `toLocal`.
 *
 */

// External to local
fun Task.toLocal() = LocalTask(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    priority = priority.name,
)

fun List<Task>.toLocal() = map(Task::toLocal)

// Local to External
fun LocalTask.toExternal() = Task(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    priority = try { Priority.valueOf(priority) } catch (_: Exception) { Priority.LOW },
)

// Note: JvmName is used to provide a unique name for each extension function with the same name.
// Without this, type erasure will cause compiler errors because these methods will have the same
// signature on the JVM.
@JvmName("localToExternal")
fun List<LocalTask>.toExternal() = map(LocalTask::toExternal)

// Network to Local
fun NetworkTask.toLocal() = LocalTask(
    id = id,
    title = title,
    description = shortDescription,
    isCompleted = (status == TaskStatus.COMPLETE),
    priority = priority?.let { networkPriorityToLocal(it) } ?: Priority.LOW.name,
)

@JvmName("networkToLocal")
fun List<NetworkTask>.toLocal() = map(NetworkTask::toLocal)

// Local to Network
fun LocalTask.toNetwork() = NetworkTask(
    id = id,
    title = title,
    shortDescription = description,
    priority = localPriorityToNetwork(priority),
    status = if (isCompleted) { TaskStatus.COMPLETE } else { TaskStatus.ACTIVE }
)

fun List<LocalTask>.toNetwork() = map(LocalTask::toNetwork)

/** Maps a network priority integer to a local priority string. */
private fun networkPriorityToLocal(value: Int): String = when (value) {
    1 -> Priority.HIGH.name
    2 -> Priority.MEDIUM.name
    else -> Priority.LOW.name
}

/** Maps a local priority string to a network priority integer. */
private fun localPriorityToNetwork(value: String): Int = when (value) {
    Priority.HIGH.name -> 1
    Priority.MEDIUM.name -> 2
    else -> 3
}

// External to Network
fun Task.toNetwork() = toLocal().toNetwork()

@JvmName("externalToNetwork")
fun List<Task>.toNetwork() = map(Task::toNetwork)

// Network to External
fun NetworkTask.toExternal() = toLocal().toExternal()

@JvmName("networkToExternal")
fun List<NetworkTask>.toExternal() = map(NetworkTask::toExternal)

