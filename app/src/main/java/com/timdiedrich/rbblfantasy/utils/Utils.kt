package com.timdiedrich.rbblfantasy.utils

import java.util.Locale
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object CountryCodeConverter {
    // Builds a map of ISO 3-letter codes ("DEU", "NLD") -> ISO 2-letter codes ("DE", "NL")
    private val alpha3ToAlpha2Map: Map<String, String> by lazy {
        Locale.getISOCountries().associateBy(
            keySelector = { countryAlpha2 ->
                Locale("", countryAlpha2).isO3Country.uppercase()
            },
            valueTransform = { countryAlpha2 -> countryAlpha2 }
        )
    }

    fun toAlpha2(alpha3: String?): String? {
        if (alpha3.isNullOrBlank()) return null
        var code = alpha3
        if(code == "IRI" ||code == "iri") code  = "IRN" // account for wrong iranian code

        // 1. Check standard ISO 3166-1 Alpha-3 map
        val isoAlpha2 = alpha3ToAlpha2Map[code.uppercase()]
        if (isoAlpha2 != null) return isoAlpha2

        // 2. Fallback for common Sports IOC codes that differ from ISO
        return when (alpha3.uppercase()) {
            "GER" -> "DE" // Germany (ISO is DEU)
            "NED" -> "NL" // Netherlands (ISO is NLD)
            "SUI" -> "CH" // Switzerland (ISO is CHE)
            "POR" -> "PT" // Portugal (ISO is PRT)
            "DEN" -> "DK" // Denmark (ISO is DNK)
            "CRO" -> "HR" // Croatia (ISO is HRV)
            "GRE" -> "GR" // Greece (ISO is GRC)
            "RSA" -> "ZA" // South Africa (ISO is ZAF)
            "CHI" -> "CL" // Chile (ISO is CHL)
            "URU" -> "UY" // Uruguay (ISO is URY)
            "NGR" -> "NG" // Nigeria (ISO is NGA)
            "MAS" -> "MY" // Malaysia (ISO is MYS)
            "FIJ" -> "FJ" // Fiji (ISO is FJI)
            else -> if (alpha3.length == 2) alpha3.uppercase() else null
        }
    }
}


fun formatMatchTime(isoString: String): String {
    return runCatching {
        val instant = Instant.parse(isoString)
        val formatter = DateTimeFormatter
            .ofPattern("EEE, dd MMM yyyy HH:mm")
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    }.getOrDefault("TBD")
}