

package org.cwcc.open.plugin.common.update.model

data class PluginVersionInfo(
    val version: String,
    val downloadUrl: String,
    val releaseDate: String,
    val changelog: List<String>
)
