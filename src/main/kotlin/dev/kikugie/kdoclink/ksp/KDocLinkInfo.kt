package dev.kikugie.kdoclink.ksp

import com.google.devtools.ksp.symbol.KSDeclaration

data class KDocLink(val url: String, val title: String)
data class KDocPoints(val host: KSDeclaration, val points: List<KDocLink>)