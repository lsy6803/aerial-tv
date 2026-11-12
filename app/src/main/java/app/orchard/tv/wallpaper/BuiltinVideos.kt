package app.orchard.tv.wallpaper

import android.content.Context
import android.util.Log
import app.orchard.tv.BuildConfig
import java.io.File

/**
 * Animations bundled with Aerial as built-in video wallpapers. Unlike the owner's videos
 * (ScreensaverVideos, sent from the phone), these ship inside the APK under assets/builtin-videos
 * and are copied out to [dir] once, so ExoPlayer can play them from a file like any other film.
 * They are offline, free, and cannot be deleted from the phone page.
 *
 * To add or rename one, edit [SPECS] (and drop the file under assets/builtin-videos). The [asset]
 * path is relative to src/main/assets.
 */
object BuiltinVideos {
    data class Spec(val key: String, val nameZh: String, val nameEn: String, val asset: String)

    /**
     * The bundled films. [key] is the wallpaper id (after the "bvid:" prefix); [asset] is the file
     * under assets/builtin-videos. Names are shown in Personalize and the phone page.
     */
    val SPECS = listOf(
        // 1794281713903.mp4 [5.00–11.00] 片段3 · 水下吐泡泡的小猫
        Spec("bubblecat", "水下小猫", "Underwater Kitten", "builtin-videos/bubblecat.mp4"),
        // 1794281713903.mp4 [11.00–15.48] 片段4 · 蜜蜂帽橘猫
        Spec("beecat", "蜜蜂橘猫", "Bee Cat", "builtin-videos/beecat.mp4"),
        // 1794281683167.mp4 [0.00–10.13] 片段5 · 3D「莫生气」西游手办（带文字气泡）
        Spec("calmwest", "莫生气", "Don't Get Angry", "builtin-videos/calmwest.mp4"),
        // 1794281649732.mp4 [3.90–7.17]  片段6 · 柴犬摇椅
        Spec("aerialfarm", "柴犬摇椅", "Shiba Rocking Chair", "builtin-videos/aerialfarm.mp4"),
        // 1794281649732.mp4 [7.17–20.40] 片段7 · 萌宠动漫（含熊猫）
        Spec("anime", "萌宠动漫", "Anime Pets", "builtin-videos/anime.mp4"),
        // 1794281649732.mp4 [20.40–23.70] 片段8 · 橘猫混剪
        Spec("orangecat", "橘猫混剪", "Orange Cat Montage", "builtin-videos/orangecat.mp4"),
        // VID_20261110_113538.mp4 [0.00–2.07]  · 3D 熊猫幼崽
        Spec("panda", "熊猫幼崽", "Panda Cub", "builtin-videos/panda.mp4"),
        // VID_20261110_113549_2.mp4 [0.00–1.30] · 3D 白色小猫（蓝眼）
        Spec("whitecat", "白色小猫", "White Kitten", "builtin-videos/whitecat.mp4"),
        // VID_20261110_113549_1.mp4 [0.00–1.30] · 3D 黑色小猫（橙眼）
        Spec("blackcat", "黑色小猫", "Black Kitten", "builtin-videos/blackcat.mp4"),
        // 1794289196801.mp4 [0.00–10.01] · 3D 举光萌兽（黑夜手电，带「举光前行 照亮黑暗」字样）
        Spec("glowbunny", "举光萌兽", "Glow Bunny", "builtin-videos/glowbunny.mp4"),
    )

    private const val TAG = "AerialBuiltin"

    private fun dir(context: Context) = File(context.filesDir, "builtin-videos").apply { mkdirs() }

    /** A bundled film's file under its own folder (so its poster sits beside it, like the owner's videos). */
    private fun filmFile(root: File, key: String) = File(File(root, key), "video.mp4")

    /** Copies every bundled film out of assets; re-copies whenever the APK version changes, so a
     *  replaced video shows up after an update without clearing app data. Safe to call repeatedly. */
    fun seed(context: Context) {
        val root = dir(context)
        val version = BuildConfig.VERSION_CODE
        val marker = File(root, "seed_version")
        val fresh = marker.isFile && marker.readText().trim() == version.toString()
        for (spec in SPECS) {
            val target = filmFile(root, spec.key)
            if (fresh && target.isFile && target.length() > 0) continue
            extract(context, root, spec)
        }
        runCatching { marker.writeText(version.toString()) }
    }

    private fun extract(context: Context, root: File, spec: Spec) {
        val target = filmFile(root, spec.key)
        runCatching {
            val part = File(root, "${spec.key}.part")
            context.assets.open(spec.asset).use { input -> part.outputStream().use { out -> input.copyTo(out) } }
            target.parentFile.mkdirs()
            check(part.renameTo(target)) { "could not rename ${spec.key}" }
        }.onFailure { Log.w(TAG, "could not seed ${spec.key}", it) }
    }

    /** The bundled films as [ScreensaverVideo]s (id = [Spec.key]), in [SPECS] order. */
    fun all(context: Context): List<ScreensaverVideo> {
        val root = dir(context)
        return SPECS.mapNotNull { spec ->
            val file = filmFile(root, spec.key)
            if (file.isFile && file.length() > 0) ScreensaverVideo(spec.key, spec.nameZh, file) else null
        }
    }

    /** The wallpaper items for the bundled films, in the current language. */
    fun specified(zh: Boolean): List<Wallpaper> =
        SPECS.map { Wallpaper(Wallpapers.BUILTIN_VIDEO_PREFIX + it.key, WallpaperKind.BVideo, if (zh) it.nameZh else it.nameEn) }

    /** A bundled film's file, or null; used by the player and frame extraction. */
    fun file(context: Context, key: String): File? {
        val f = filmFile(dir(context), key)
        return if (f.isFile && f.length() > 0) f else null
    }
}
