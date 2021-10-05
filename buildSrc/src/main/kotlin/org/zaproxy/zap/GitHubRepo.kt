package org.zaproxy.zap

import java.io.File

data class GitHubRepo(val owner: String, val name: String, val dir: File? = null) {

    override fun toString() = "$owner/$name"
}
