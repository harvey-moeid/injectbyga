package com.byga.flowmanager.organizer.domain

import com.byga.flowmanager.domain.IdentityRules
import java.util.Locale

/** Bounded RFC-style CSV reader. Never imports credentials or renders rejected row contents. */
object AccountImport {
    const val MAX_BYTES = 2 * 1024 * 1024
    const val MAX_ROWS = 5000
    private data class Row(val line: Int, val cells: List<String>)

    fun preview(text: String, csv: Boolean, existing: Set<String>): ImportPreview {
        require(text.toByteArray(Charsets.UTF_8).size <= MAX_BYTES) { "File maksimal 2 MB." }
        val source = text.removePrefix("\uFEFF")
        val rows = if (csv) parseCsv(source) else source.lineSequence().mapIndexedNotNull { i, line ->
            if (line.isBlank()) null else Row(i + 1, listOf(line.trim()))
        }.take(MAX_ROWS + 1).toList()
        val indices: Map<String, Int>
        val records: List<Row>
        if (csv) {
            require(rows.isNotEmpty()) { "File CSV kosong." }
            val headers = rows.first().cells.map { it.trim().lowercase(Locale.ROOT) }
            require(headers.size == headers.toSet().size && headers.contains("email") &&
                headers.all { it in setOf("email", "alias", "group") }) {
                "Header CSV harus email,alias,group (alias/group opsional). Kolom password, OTP, cookie atau token tidak didukung."
            }
            indices = headers.withIndex().associate { it.value to it.index }
            records = rows.drop(1)
        } else {
            indices = mapOf("email" to 0)
            records = rows
        }
        require(records.size <= MAX_ROWS) { "Maksimal 5.000 akun per impor." }
        val seen = existing.map(IdentityRules::normalize).toMutableSet()
        val valid = mutableListOf<ImportCandidate>()
        val rejected = mutableListOf<ImportIssue>()
        val duplicates = mutableListOf<ImportIssue>()
        for (row in records) {
            if (row.cells.size != indices.size) { rejected += ImportIssue(row.line, "Jumlah kolom tidak sesuai header"); continue }
            fun field(name: String): String = indices[name]?.let { row.cells[it].trim() }.orEmpty()
            val email = IdentityRules.normalize(field("email"))
            val alias = field("alias").ifEmpty { OrganizerRules.defaultAlias(email) }
            val group = field("group")
            if (!IdentityRules.isValid(email)) { rejected += ImportIssue(row.line, "Email tidak valid"); continue }
            if (alias.length !in 1..80 || alias.any { it.isISOControl() } ||
                (group.isNotEmpty() && !OrganizerRules.validGroup(group))) {
                rejected += ImportIssue(row.line, "Alias/grup tidak valid atau terlalu panjang"); continue
            }
            if (!seen.add(email)) { duplicates += ImportIssue(row.line, "Email duplikat"); continue }
            valid += ImportCandidate(email, alias, group)
        }
        return ImportPreview(valid, rejected, duplicates, records.size)
    }

    fun csv(accounts: List<ManagedAccount>, groups: List<AccountGroup>): String {
        val names = groups.associate { it.id to it.name }
        fun cell(value: String): String {
            val safe = if (value.trimStart().firstOrNull() in listOf('=', '+', '-', '@') ||
                value.startsWith('\t') || value.startsWith('\r')) "'$value" else value
            return "\"${safe.replace("\"", "\"\"")}\""
        }
        return buildString {
            append("email,alias,group\r\n")
            accounts.forEach { a -> append(listOf(a.email, a.alias, names[a.groupId].orEmpty()).joinToString(",") { cell(it) }); append("\r\n") }
        }
    }

    private fun parseCsv(text: String): List<Row> {
        val result = mutableListOf<Row>()
        val cells = mutableListOf<String>()
        val field = StringBuilder()
        var quoted = false
        var closedQuote = false
        var line = 1
        var rowLine = 1
        var i = 0
        fun addCell() {
            cells += field.toString(); field.setLength(0); closedQuote = false
            require(cells.size <= 32) { "Terlalu banyak kolom CSV." }
        }
        fun addRow() {
            addCell()
            if (cells.any { it.isNotBlank() }) result += Row(rowLine, cells.toList())
            cells.clear()
            require(result.size <= MAX_ROWS + 1) { "Maksimal 5.000 akun per impor." }
        }
        while (i < text.length) {
            val c = text[i]
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < text.length && text[i + 1] == '"') { field.append('"'); i++ }
                    else { quoted = false; closedQuote = true }
                } else { field.append(c); if (c == '\n') line++ }
            } else when (c) {
                '"' -> { require(field.isEmpty() && !closedQuote) { "Format kutipan CSV tidak valid." }; quoted = true }
                ',' -> addCell()
                '\r', '\n' -> {
                    addRow()
                    if (c == '\r' && i + 1 < text.length && text[i + 1] == '\n') i++
                    line++; rowLine = line
                }
                else -> {
                    require(!closedQuote || c == ' ' || c == '\t') { "Karakter setelah kutipan CSV tidak valid." }
                    if (!closedQuote) field.append(c)
                }
            }
            i++
        }
        require(!quoted) { "Kutipan CSV belum ditutup." }
        if (field.isNotEmpty() || cells.isNotEmpty() || closedQuote) addRow()
        return result
    }
}
