package com.clearphone.launcher

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

// ---------------------------------------------------------------------------
// Icon pack support — ADW-style packs (assets/appfilter.xml or res/xml/appfilter)
//
//   <item component="ComponentInfo{pkg/activity}" drawable="my_drawable" />
//
// Detection is via the standard theme intents that icon packs declare. Each
// app's launcher component is matched against the pack's appfilter; if there's
// no exact component match we fall back to a package-name match, then to the
// app's own stock icon.
// ---------------------------------------------------------------------------

data class IconPackInfo(val packageName: String, val label: String)

/** A loaded icon pack: its resources plus the parsed component/package → drawable maps. */
class IconPack(
    val packageName: String,
    private val resources: Resources,
    private val componentMap: Map<String, String>,
    private val packageMap: Map<String, String>
) {
    // Cache resolved drawables per app package so we don't re-inflate on every recomposition.
    private val cache = HashMap<String, Drawable?>()

    val size: Int get() = componentMap.size

    /** Returns the pack drawable for this app, or null if the pack has no match. */
    fun getIcon(app: AppItem): Drawable? {
        cache[app.packageName]?.let { return it }
        if (cache.containsKey(app.packageName)) return null   // cached miss

        val drawableName = componentMap[app.component] ?: packageMap[app.packageName]
        val drawable: Drawable? = if (drawableName == null) {
            null
        } else {
            val id = resources.getIdentifier(drawableName, "drawable", packageName)
            if (id == 0) null
            else try {
                // mutate() so two apps sharing a drawable don't share mutable bounds/state
                ResourcesCompat.getDrawable(resources, id, null)?.mutate()
            } catch (e: Exception) {
                null
            }
        }
        cache[app.packageName] = drawable
        return drawable
    }
}

class IconPackManager(private val context: Context) {

    private val themeIntents = listOf(
        "org.adw.launcher.THEMES",
        "com.gau.go.launcherex.theme",
        "com.novalauncher.THEME"
    )

    /** All installed icon packs (deduplicated by package), sorted by label. */
    fun getAvailableIconPacks(): List<IconPackInfo> {
        val pm = context.packageManager
        val found = LinkedHashMap<String, IconPackInfo>()
        for (action in themeIntents) {
            val resolved = try {
                pm.queryIntentActivities(Intent(action), 0)
            } catch (e: Exception) {
                emptyList()
            }
            for (ri in resolved) {
                val pkg = ri.activityInfo.packageName
                if (!found.containsKey(pkg)) {
                    val label = try { ri.loadLabel(pm).toString() } catch (e: Exception) { pkg }
                    found[pkg] = IconPackInfo(pkg, label)
                }
            }
        }
        return found.values.sortedBy { it.label.lowercase() }
    }

    /** Loads and parses a pack's appfilter. Returns null if the pack/appfilter can't be read. */
    fun load(packageName: String): IconPack? {
        if (packageName.isBlank()) return null
        val pm = context.packageManager
        val res = try {
            pm.getResourcesForApplication(packageName)
        } catch (e: Exception) {
            return null
        }

        val componentMap = HashMap<String, String>()
        val packageMap = HashMap<String, String>()

        // Prefer assets/appfilter.xml; fall back to res/xml/appfilter. Track both so we can close them.
        var assetStream: java.io.InputStream? = null
        var xmlRes: android.content.res.XmlResourceParser? = null
        val parser: XmlPullParser? = try {
            assetStream = res.assets.open("appfilter.xml")
            XmlPullParserFactory.newInstance().newPullParser().apply { setInput(assetStream, "UTF-8") }
        } catch (e: Exception) {
            assetStream = null
            try {
                val id = res.getIdentifier("appfilter", "xml", packageName)
                if (id != 0) res.getXml(id).also { xmlRes = it } else null
            } catch (e2: Exception) {
                null
            }
        }
        if (parser == null) return null

        try {
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && parser.name == "item") {
                    val component = parser.getAttributeValue(null, "component")
                    val drawable = parser.getAttributeValue(null, "drawable")
                    if (!component.isNullOrEmpty() && !drawable.isNullOrEmpty()) {
                        componentMap[component] = drawable
                        packageFromComponent(component)?.let { pkg ->
                            if (!packageMap.containsKey(pkg)) packageMap[pkg] = drawable
                        }
                    }
                }
                event = parser.next()
            }
        } catch (e: Exception) {
            // Partial parse is still usable; fall through with whatever we collected.
        } finally {
            try { assetStream?.close() } catch (e: Exception) {}
            try { xmlRes?.close() } catch (e: Exception) {}
        }

        if (componentMap.isEmpty()) return null
        return IconPack(packageName, res, componentMap, packageMap)
    }

    // "ComponentInfo{com.whatsapp/com.whatsapp.HomeActivity}" -> "com.whatsapp"
    private fun packageFromComponent(component: String): String? {
        val open = component.indexOf('{')
        val slash = component.indexOf('/')
        return if (open in 0 until slash) component.substring(open + 1, slash) else null
    }
}
